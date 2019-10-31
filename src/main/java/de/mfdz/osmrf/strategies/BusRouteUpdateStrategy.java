package de.mfdz.osmrf.strategies;

import com.google.common.collect.Maps;
import com.graphhopper.GHRequest;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.mfdz.osmrf.role.EmptyRole;
import de.mfdz.osmrf.role.RoleStrategy;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.RelationMember;

import java.util.*;

public class BusRouteUpdateStrategy extends AbstractRouteUpdateStrategy {

    private static final RoleStrategy ROLE_STRATEGY = new EmptyRole();

    private static final Set<String> WAY_ROLES = new HashSet<>(
            Arrays.asList("", "forward", "backward"));

    private static final HashMap<String, String> TAG_TEMPLATE = new HashMap<>();
    static {
        TAG_TEMPLATE.put("type", "route");
        TAG_TEMPLATE.put("route", "bus");
        TAG_TEMPLATE.put("public_transport:version", "2");
        TAG_TEMPLATE.put("ref", "");
        TAG_TEMPLATE.put("from", "");
        TAG_TEMPLATE.put("to", "");
        TAG_TEMPLATE.put("name", "");
        TAG_TEMPLATE.put("operator", "");
        TAG_TEMPLATE.put("network", "");
    }

    @Override
    public List<GHPoint> getRoutePoints(CachedRelationWrapper rel) {
        List<GHPoint> routePoints = new ArrayList<>();
        int count = 0;
        for (RelationMember member : rel.getMembers()) {
            if ("stop".equals(member.getRole())) {
                count++;
                GHPoint point = asGHPoint(rel.getMemberElement(member));
                routePoints.add(point);
              }
        }

        //routePoints.add(new GHPoint(48.69203, 9.12266));
        return routePoints;
    }

    @Override
    public RoleStrategy getRoleStrategy() {
        return ROLE_STRATEGY;
    }

    @Override
    public Set<String> getWayRoles() {
        return WAY_ROLES;
    }

    @Override
    public String getNodeRole(int i, int size) {
        // FIXME need tags of node to estimate role... if stop_position, role is stop, else viaPoint
        return "stop";
    }

    @Override
    public Map<String, String> getTagTemplate() {
        return Maps.newHashMap(TAG_TEMPLATE);
    }

    @Override
    public String getRouteType() {
        return "bus";
    }

    @Override
    public GHRequest getRouteRequest(List<GHPoint> routePoints) {
        GHRequest request =  new GHRequest(routePoints).
                setWeighting("fastest").setVehicle("bus");
        request.getHints().put("edge_based", Boolean.TRUE).put("u_turn_costs", -1)
                .put("pass_through", Boolean.TRUE)
        ;
        return request;
    }

    @Override
    public void updateWayMembersIfNecessary(CachedRelationWrapper relation,
            List<RelationMember> wayMembers) {
        // always?
        wayMembers.remove(0);
        super.updateWayMembersIfNecessary(relation, wayMembers );
    }
}
