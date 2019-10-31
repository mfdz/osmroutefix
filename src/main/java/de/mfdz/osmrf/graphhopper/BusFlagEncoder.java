package de.mfdz.osmrf.graphhopper;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;

import java.util.Arrays;

/**
 * Defines bit layout for busses. (speed, access, ferries, ...)
 * Note: this is not the the official Graphhopper BusFlagEncoder.
 * It is currently very simple and will need further improvement.
 *
 * @author Holger Bruch
 */
public class BusFlagEncoder extends CarFlagEncoder
{
    public BusFlagEncoder(PMap properties) {
        super(properties);
        // Need to clear all restrictions, so we can add priority sorted keys
        this.restrictions.clear();
        this.restrictions.addAll(Arrays.asList("bus", "psv", "motor_vehicle", "vehicle", "access"));

        absoluteBarriers.remove("bus_trap");
        absoluteBarriers.remove("sump_buster");
    }

    @Override
    public String toString() {
        return "bus";
    }
}