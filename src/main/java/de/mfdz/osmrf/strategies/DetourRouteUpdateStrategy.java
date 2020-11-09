package de.mfdz.osmrf.strategies;

import com.google.common.collect.Maps;
import com.graphhopper.GHRequest;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.mfdz.osmrf.role.ForwardBackwardRole;
import de.mfdz.osmrf.role.RoleStrategy;
import de.mfdz.osmrf.validation.ComparisonResult;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

import java.util.*;

public class DetourRouteUpdateStrategy extends AbstractRouteUpdateStrategy {

    /**
     * For detour routes, the way members are added with their orientation (backword/forward).
     */
    private static final RoleStrategy ROLE_STRATEGY = new ForwardBackwardRole();

    /**
     * "", "forward" and "backward" are the roles expected as detour route way
     * members which will be updated.
     */
    private static final Set<String> WAY_ROLES = new HashSet<>(
            Arrays.asList("", "forward", "backward"));

    private static final HashMap<String, String> TAG_TEMPLATE = new HashMap<>();
    static {
        TAG_TEMPLATE.put("type", "route");
        TAG_TEMPLATE.put("route", "detour");
        TAG_TEMPLATE.put("ref", "");
        TAG_TEMPLATE.put("detour", "");
        TAG_TEMPLATE.put("name", "");
        TAG_TEMPLATE.put("operator", "");
        TAG_TEMPLATE.put("destination", "");
    }

    @Override
    public List<GHPoint> getRoutePoints(CachedRelationWrapper rel) {
        GHPoint start = null;
        List<GHPoint> viaPoints = new ArrayList<>();
        GHPoint end = null;
        for (RelationMember member : rel.getMembers()) {
            if ("start".equals(member.getRole())) {
                start = asGHPoint(rel.getMemberElement(member));
            } else if ("end".equals(member.getRole())) {
                end = asGHPoint(rel.getMemberElement(member));
            } else if ("viaNode".equals(member.getRole())) {
                viaPoints.add(asGHPoint(rel.getMemberElement(member)));
            }
        }

        List<GHPoint> routePoints = new ArrayList<>();
        routePoints.add(start);
        routePoints.addAll(viaPoints);
        routePoints.add(end);

        return routePoints;
    }

    @Override
    public RoleStrategy getRoleStrategy() {
        return ROLE_STRATEGY;
    }

    @Override
    public String getNodeRole(int pos, int size) {
        if (pos <= 0) return "start";
        else if (pos >= size-1) return "end";
        else return "viaNode";
    }

    @Override
    public Set<String> getWayRoles() {
        return WAY_ROLES;
    }

    @Override
    public Map<String, String> getTagTemplate() {
        return Maps.newHashMap(TAG_TEMPLATE);
    }

    @Override
    public String getRouteType() {
        return "detour";
    }

    @Override
    public GHRequest getRouteRequest(List<GHPoint> routePoints) {
        GHRequest request =  new GHRequest(routePoints).
                setWeighting("fastest").setVehicle("car");
        request.getSnapPreventions().add("link");
        request.getHints().put("pass_through", Boolean.TRUE);
        return request;
    }

    @Override
    public ComparisonResult validate(CachedRelationWrapper rel, MapDataDao dao) {
        ComparisonResult result = super.validate(rel, dao);

        Way lastWay = null, currentWay;
        RelationMember start = rel.getFirstMemberWithRole("start");
        RelationMember end = rel.getFirstMemberWithRole("end");
        if (start == null || start.getType() != Element.Type.NODE) {
            result.addIssue("noStartNode", "Relation has no node member with role start");
        } else {
            Node startNode = (Node) rel.getMemberElement(start);
            if (startNode.getTags() == null || !"motorway_junction".equals(startNode.getTags().get("highway"))) {
                result.addIssue("startNodeNoJunction",
                        "n" + startNode.getId() + " is no motorway_junction");
            }
        }

        if (end == null || end.getType() != Element.Type.NODE) {
            result.addIssue("noEndNode", "Relation has no node member with role end");
        } else {
            Node endNode = (Node) rel.getMemberElement(end);
            List<Way> waysAtLastNode = dao.getWaysForNode(endNode.getId());
            if (waysAtLastNode.size() < 3) {
                result.addIssue("endNodeWaysCount",
                        "n" + endNode.getId() + " has only " + waysAtLastNode.size() + " connected ways");
            }
        }

        for (RelationMember member : rel.getMembers()) {
            if (member.getType() == Element.Type.WAY) {
                currentWay = (Way) rel.getMemberElement(member);
                if (lastWay == null) {
                    if (currentWay.getTags() == null || !"motorway_link".equals(currentWay.getTags().get("highway"))) {
                        result.addIssue("firstWayNoLink",
                                "w" + currentWay.getId() + " is no motorway_link");
                    }
                }
                lastWay = currentWay;
            }
        }

        if (!"motorway_link".equals(lastWay.getTags().get("highway"))) {
            result.addIssue("lastWayNoLink", "w" + lastWay.getId() + " is no motorway_link");
        }

        return result;
    }
}
