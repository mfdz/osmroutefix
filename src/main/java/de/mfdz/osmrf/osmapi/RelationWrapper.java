package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

import java.util.List;
import java.util.stream.Collectors;

public class RelationWrapper extends ElementWrapper implements Relation {

    private Relation relation;

    public RelationWrapper(Relation relation) {
        super(relation);
        this.relation = relation;
    }

    @Override
    public List<RelationMember> getMembers() {
        return relation.getMembers();
    }

    public List<RelationMember> getMembersOfType(Element.Type type) {
        return relation.getMembers().stream().sequential().filter(member -> member.getType() == type)
                .collect(Collectors.toList());
    }

    public RelationMember getFirstMemberWithRole(String role) {
        for (RelationMember member: getMembers()) {
            if (member.getRole().equals(role))
                return member;
        }
        return null;
    }
}
