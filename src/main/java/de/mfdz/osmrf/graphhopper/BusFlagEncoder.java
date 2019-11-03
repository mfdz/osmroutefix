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

        intendedValues.add("designated");
        absoluteBarriers.remove("bus_trap");
        absoluteBarriers.remove("sump_buster");
        defaultSpeedMap.put("pedestrian", 6);
    }

    @Override
    protected boolean isOneway(ReaderWay way) {
        return !(way.hasTag("oneway:bus", "no") || way.hasTag("oneway:psv", "no")) && super.isOneway(way);
    }

    @Override
    public EncodingManager.Access getAccess(ReaderWay way) {
         String highwayValue = way.getTag("highway");
         String firstValue = way.getFirstPriorityTag(this.restrictions);
         if ("pedestrian".equals(highwayValue)) {
             return intendedValues.contains(firstValue) ? EncodingManager.Access.WAY : EncodingManager.Access.CAN_SKIP;
         } else {
             return super.getAccess(way);
         }
    }

    @Override
    public String toString() {
        return "bus";
    }
}