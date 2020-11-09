package de.mfdz.osmrf.strategies;

import com.google.common.collect.Maps;
import com.graphhopper.GHRequest;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.mfdz.osmrf.role.EmptyRole;
import de.mfdz.osmrf.role.RoleStrategy;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.RelationMember;

import java.util.*;

public class BicycleRouteUpdateStrategy extends AbstractRouteUpdateStrategy {

    private static final RoleStrategy ROLE_STRATEGY = new EmptyRole();

    private static final Set<String> WAY_ROLES = new HashSet<>(
            Arrays.asList("", "forward", "backward", "main", "alternative", "excursion", "connection"));

    private static final HashMap<String, String> TAG_TEMPLATE = new HashMap<>();
    static {
        TAG_TEMPLATE.put("type", "route");
        TAG_TEMPLATE.put("route", "bicycle");
        TAG_TEMPLATE.put("ref", "");
        TAG_TEMPLATE.put("name", "");
        TAG_TEMPLATE.put("operator", "");
        TAG_TEMPLATE.put("network", "");
        TAG_TEMPLATE.put("distance", "");
        TAG_TEMPLATE.put("ascent", "");
        TAG_TEMPLATE.put("descent", "");
        TAG_TEMPLATE.put("roundtrip", "");
        TAG_TEMPLATE.put("signed_direction", "");
    }

    @Override
    public List<GHPoint> getRoutePoints(CachedRelationWrapper rel) {
        List<GHPoint> routePoints = new ArrayList<>();
        for (RelationMember member : rel.getMembers()) {
            if (member.getType() == Element.Type.NODE) {
                routePoints.add(asGHPoint(rel.getMemberElement(member)));
            }
        }

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
        return "";
    }

    @Override
    public Map<String, String> getTagTemplate() {
        return Maps.newHashMap(TAG_TEMPLATE);
    }

    @Override
    public String getRouteType() {
        return "bicycle";
    }

    @Override
    public GHRequest getRouteRequest(List<GHPoint> routePoints) {
        GHRequest request =  new GHRequest(routePoints).
                setWeighting("fastest").setVehicle("bike");
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
