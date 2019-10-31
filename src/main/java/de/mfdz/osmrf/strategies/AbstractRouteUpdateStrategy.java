package de.mfdz.osmrf.strategies;

import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.RelationMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractRouteUpdateStrategy implements RouteUpdateStrategy {

    final Logger logger = LoggerFactory.getLogger(AbstractRouteUpdateStrategy.class);


    GHPoint asGHPoint(Element node) {
        if (node.getType() != Element.Type.NODE) {
            throw new IllegalArgumentException(
                    "Unexpected element type " + node.getType() + "for " + node.getId()
                            + ". Should be NODE.");
        }
        return new GHPoint(((Node) node).getPosition().getLatitude(), ((Node) node).getPosition().getLongitude());
    }

    private void removeWayMembers(CachedRelationWrapper relation) {
        Iterator<RelationMember> iterator = relation.getMembers().iterator();
        while (iterator.hasNext()) {
            RelationMember member = iterator.next();
            if (member.getType() == Element.Type.WAY && getWayRoles().contains(member.getRole())) {
                iterator.remove();
            }
        }
    }

    @Override
    public void updateWayMembersIfNecessary(CachedRelationWrapper relation,
            List<RelationMember> wayMembers) {
        boolean equal = areWayMembersEqual(relation, wayMembers);

        if (!equal) {
            removeWayMembers(relation);
            relation.getMembers().addAll(wayMembers);
        }
    }

    private boolean areWayMembersEqual(CachedRelationWrapper relation,
            List<RelationMember> wayMembers) {
        Iterator<RelationMember> iterator = relation.getMembers().iterator();
        int wayIndex = 0;
        boolean equal = true;
        boolean wayEncountered = false;
        while (iterator.hasNext()) {
            RelationMember member = iterator.next();
            // skip irrelevant way members at the beginning, assuming route members come last...
            if (!wayEncountered && member.getType() != Element.Type.WAY || !getWayRoles().contains(member.getRole())) {
                continue;
            }
            wayEncountered = true;
            // Now we encounter first way, we compare if all members are equal to wayMembers
            if (wayMembers.size() < wayIndex + 1) {
                equal = false;
                logger.debug("Relation {} differs at member index {}. {}{} (role: {}) is unmatched.",
                        relation.getId(),
                        wayIndex,
                        member.getType(),
                        member.getRef(),
                        member.getRole());
                break;
            }  else if (!wayMembers.get(wayIndex).equals(member)) {
                equal = false;
                logger.debug("Relation {} differs at member index {}. Existing has w{} (role: {}), updated w{} (role: {})",
                        relation.getId(),
                        wayIndex,
                        member.getRef(),
                        member.getRole(),
                        wayMembers.get(wayIndex).getRef(),
                        wayMembers.get(wayIndex).getRole()
                        );
            }
            wayIndex++;
        }
        return equal;
    }
}
