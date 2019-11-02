package de.mfdz.osmrf.graphhopper;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BusFlagEncoderTest {
    private final EncodingManager em = EncodingManager.create(Arrays.asList(
            new BusFlagEncoder(new PMap("speed_two_directions=true"))), 8);
    private final BusFlagEncoder encoder = (BusFlagEncoder) em.getEncoder("bus");
    private final BooleanEncodedValue accessEnc = encoder.getAccessEnc();

    @Test
    public void testGetAccess() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "tertiary");
        way.setTag("motorcar", "no");
        way.setTag("bus", "yes");
        assertTrue(encoder.getAccess(way).isWay());
    }

    @Test
    public void testOneway() {
        // e.g. w445054512
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "tertiary");
        way.setTag("oneway", "yes");
        way.setTag("oneway:bus", "no");
        way.setTag("bus:backward", "designated");
        IntsRef flags = encoder.handleWayTags(em.createEdgeFlags(), way, encoder.getAccess(way), 0);
        assertTrue(accessEnc.getBool(false, flags));
        assertTrue(accessEnc.getBool(true, flags));
        way.clearTags();
    }
}

