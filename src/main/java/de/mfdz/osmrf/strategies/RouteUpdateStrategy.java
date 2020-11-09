package de.mfdz.osmrf.strategies;

import com.graphhopper.GHRequest;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.validation.ComparisonResult;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.mfdz.osmrf.role.RoleStrategy;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.RelationMember;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RouteUpdateStrategy {
    String getNodeRole(int i, int size);

    List<GHPoint> getRoutePoints(CachedRelationWrapper rel);

    RoleStrategy getRoleStrategy();

    Set<String> getWayRoles();

    Map<String, String> getTagTemplate();

    void updateWayMembersIfNecessary(CachedRelationWrapper relation,
            List<RelationMember> wayMembers);

    String getRouteType();

    GHRequest getRouteRequest(List<GHPoint> routePoints);

    ComparisonResult areWayMembersEqual(CachedRelationWrapper relation, List<RelationMember> wayMembers);

    ComparisonResult validate(CachedRelationWrapper relation, MapDataDao dao);
}
