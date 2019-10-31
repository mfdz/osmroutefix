package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.map.changes.MapDataChangesWriter;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Relation;

import javax.ws.rs.core.StreamingOutput;
import java.util.List;

public class StreamingMapDataChangesWriter extends MapDataChangesWriter implements StreamingOutput {
    public StreamingMapDataChangesWriter(long changesetId, Iterable<Element> elements) {
        super(changesetId, elements);
    }
}
