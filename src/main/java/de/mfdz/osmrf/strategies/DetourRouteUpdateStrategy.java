package de.mfdz.osmrf.strategies;

import com.google.common.collect.Maps;
import com.graphhopper.GHRequest;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.mfdz.osmrf.role.ForwardBackwardRole;
import de.mfdz.osmrf.role.RoleStrategy;
import de.westnordost.osmapi.map.data.RelationMember;

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
        return request;
    }


}
