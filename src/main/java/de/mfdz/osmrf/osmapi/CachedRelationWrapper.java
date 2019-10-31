package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

public class CachedRelationWrapper extends RelationWrapper {
    private CachingMapDataHandler handler;

    public CachedRelationWrapper(Relation relation, CachingMapDataHandler handler) {
        super(relation);
        this.handler = handler;
    }

    public Element getMemberElement(RelationMember member) {
        if (member.getType() == Type.NODE) {
            return handler.getNode(member.getRef());
        } else if (member.getType() == Type.WAY) {
            return handler.getWay(member.getRef());
        } else {
            return handler.getRelation(member.getRef());
        }
    }
}
