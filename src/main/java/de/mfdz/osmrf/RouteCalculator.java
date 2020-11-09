package de.mfdz.osmrf;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.exceptions.PointOutOfBoundsException;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.graphhopper.RouteFixHopper;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.mfdz.osmrf.osmapi.CachingMapDataHandler;
import de.mfdz.osmrf.osmapi.RelationWrapper;
import de.mfdz.osmrf.role.RoleStrategy;
import de.mfdz.osmrf.strategies.BusRouteUpdateStrategy;
import de.mfdz.osmrf.strategies.DetourRouteUpdateStrategy;
import de.mfdz.osmrf.strategies.RouteUpdateStrategy;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.*;

import java.util.ArrayList;
import java.util.List;

public class RouteCalculator {

    private final OsmConnection osm;

    private final RouteFixHopper gh;

    private RouteUpdateStrategy[] updateStrategies = { new DetourRouteUpdateStrategy(), new BusRouteUpdateStrategy() };

    public RouteCalculator( OsmConnection osm, RouteFixHopper gh) {
        this.osm = osm;
        this.gh = gh;
    }


    private RouteUpdateStrategy getRouteUpdateStrategy(CachedRelationWrapper relation) {
        if (!"route".equals(relation.getTag("type"))) {
            throw new IllegalArgumentException("Can't update relation "+ relation + " as not of type route");
        }
        String route = relation.getTag("route");
        RouteUpdateStrategy strategy = getRouteUpdateStrategy(route);
        if (strategy == null)
            throw new UnsupportedOperationException("Can't update route relation "+ relation + ": no RouteUpdateStrategy registered for route="+route);
        return strategy;
    }

    private RouteUpdateStrategy getRouteUpdateStrategy(String route) {
        for (RouteUpdateStrategy strategy: updateStrategies) {
            if (strategy.getRouteType().equals(route)) {
                return strategy;
            }
        }
        return null;
    }

    public GHRequest existingRouteRequest(CachedRelationWrapper routeRelation) {
        RouteUpdateStrategy strategy = getRouteUpdateStrategy(routeRelation);

        List<GHPoint> routePoints = strategy.getRoutePoints(routeRelation);

        return strategy.getRouteRequest(routePoints);
    }

    public Relation getUpdatedRoute(long relId) {
        CachedRelationWrapper relation = getRouteRelation(relId);

        GHRequest request = existingRouteRequest(relation);

        List<Path> paths = getPaths(request);
        RouteUpdateStrategy strategy = getRouteUpdateStrategy(relation);
        List<RelationMember> wayMembers = getWayMembers(paths, strategy.getRoleStrategy());

        strategy.updateWayMembersIfNecessary(relation, wayMembers);
        return relation;
    }

    private List<RelationMember> getNodeMembers(
            List<GHPoint> points, RouteUpdateStrategy roleStrategy) {
        List<RelationMember> newMembers = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            GHPoint point = points.get(i);
            QueryResult closest = gh.getLocationIndex().findClosest(point.lat, point.lon, EdgeFilter.ALL_EDGES);
            int closestNode = closest.getClosestNode();
            long node = gh.getOSMNode(closestNode);
            String role = roleStrategy.getNodeRole(i, points.size());
            newMembers.add(new OsmRelationMember(node, role, Element.Type.NODE));
        }
        return newMembers;
    }

    private List<RelationMember> getWayMembers(
            List<Path> paths, RoleStrategy roleStrategy) {
        long osmWay = 0L;
        List<RelationMember> newMembers = new ArrayList<>();

        for (Path path : paths) {
            for (EdgeIteratorState edge : path.calcEdges()) {
                int edgeId = edge.getEdge();
                if (edge instanceof VirtualEdgeIteratorState) {
                    // first, via and last edges can be virtual
                    VirtualEdgeIteratorState vEdge = (VirtualEdgeIteratorState) edge;
                    edgeId = vEdge.getOriginalEdgeKey() / 2;
                }
                if (osmWay != gh.getOSMWay(edgeId)) {
                    osmWay = gh.getOSMWay(edgeId);
                    String role = roleStrategy.getRole(edge);
                    newMembers.add(new OsmRelationMember(osmWay, role, Element.Type.WAY));
                }
            }
        }
        return newMembers;
    }

    public CachedRelationWrapper getRouteRelation(long relId) {
        MapDataDao mapDao = new MapDataDao(osm);
        CachingMapDataHandler handler = new CachingMapDataHandler();
        mapDao.getRelationComplete(relId, handler);
        OsmRelation r = handler.getRelation(relId);
        return new CachedRelationWrapper(r, handler);
    }

    private List<Path> getPaths(GHRequest request) {
        GHResponse rsp = new GHResponse();
        List<Path> paths = gh.calcPaths(request, rsp);
        if (!rsp.getErrors().isEmpty()) {
            throw new RuntimeException((rsp.getErrors().get(0)));
        }
        return paths;
    }

    public GHRequest newRouteRequest(String routeType, List<GHPoint> routePoints) {
        RouteUpdateStrategy strategy = getRouteUpdateStrategy(routeType);
        if (strategy == null)
            throw new IllegalArgumentException("No RouteUpdateStrategy registered for route="+routeType);

        return strategy.getRouteRequest(routePoints);
    }

    public RelationWrapper newRoute(String routeType, List<GHPoint> routePoints) {
        RouteUpdateStrategy strategy = getRouteUpdateStrategy(routeType);
        List<RelationMember> members = getNodeMembers(routePoints, strategy);

        List<RelationMember> wayMembers = computeWayMembers(routeType, routePoints);
        members.addAll(wayMembers);
        return new RelationWrapper(new OsmRelation(-1, 0, members, strategy.getTagTemplate()));
    }

    private List<RelationMember> computeWayMembers(String routeType, List<GHPoint> routePoints) {
        RouteUpdateStrategy strategy = getRouteUpdateStrategy(routeType);
        GHRequest request = newRouteRequest(routeType, routePoints);
        List<Path> paths = getPaths(request);
        List<RelationMember> wayMembers = getWayMembers(paths, strategy.getRoleStrategy());
        return filterStartEndWayByRoutePoints(wayMembers, routePoints.get(0), routePoints.get(routePoints.size()-1));
    }

    private List<RelationMember> filterStartEndWayByRoutePoints(List<RelationMember> wayMembers, GHPoint startPoint, GHPoint endPoint) {
        int waysSize = wayMembers.size();
        int startSeq = distance(wayMembers.get(0), startPoint) >= distance(wayMembers.get(1), startPoint)? 1 : 0;
        int endSeq = distance(wayMembers.get(waysSize-1), endPoint) >= distance(wayMembers.get(waysSize-2), endPoint)? waysSize - 1 : waysSize;
        return wayMembers.subList(startSeq, endSeq);
    }

    private double distance(RelationMember relationMember, GHPoint coords) {
        MapDataDao mapDao = new MapDataDao(osm);
        CachingMapDataHandler handler = new CachingMapDataHandler();
        mapDao.getWayComplete(relationMember.getRef(), handler);
        Way nodeIds = handler.getWay(relationMember.getRef());
        nodeIds.getNodeIds();
        Node startNode = handler.getNode(nodeIds.getNodeIds().get(0));
        Node endNode = handler.getNode(nodeIds.getNodeIds().get(nodeIds.getNodeIds().size()-1));

        return Math.min(distance(coords.lat, coords.lon, startNode.getPosition().getLatitude(), startNode.getPosition().getLongitude()),
                distance(coords.lat, coords.lon, endNode.getPosition().getLatitude(), endNode.getPosition().getLongitude()));
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math
                .cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}
