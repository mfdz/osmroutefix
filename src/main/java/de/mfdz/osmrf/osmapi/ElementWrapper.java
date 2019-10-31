package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.changesets.Changeset;
import de.westnordost.osmapi.map.data.Element;

import java.util.Date;
import java.util.Map;

public class ElementWrapper implements Element {

    private Element element;

    public ElementWrapper(Element wrappedElement) {
        element = wrappedElement;
    }

    @Override
    public boolean isNew() {
        return element.isNew();
    }

    @Override
    public boolean isModified() {
        return element.isModified();
    }

    @Override
    public boolean isDeleted() {
        return element.isDeleted();
    }

    @Override
    public long getId() {
        return element.getId();
    }

    @Override
    public int getVersion() {
        return element.getVersion();
    }

    @Override
    public Changeset getChangeset() {
        return element.getChangeset();
    }

    @Override
    public Date getDateEdited() {
        return element.getDateEdited();
    }

    @Override
    public Map<String, String> getTags() {
        return element.getTags();
    }

    @Override
    public Type getType() {
        return element.getType();
    }

    public String getTag(String key) {
        return element.getTags().get(key);
    }
}
