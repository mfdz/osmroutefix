package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

import java.util.List;

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

    public RelationMember getFirstMemberWithRole(String role) {
        for (RelationMember member: getMembers()) {
            if (member.getRole().equals(role))
                return member;
        }
        return null;
    }
}
