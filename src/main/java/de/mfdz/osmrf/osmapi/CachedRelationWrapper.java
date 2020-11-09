package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<Element> getMemberElementsOfType(Element.Type type) {
        return getMembersOfType(type).stream().sequential().map(member -> getMemberElement(member))
                .collect(Collectors.toList());
    }
}
