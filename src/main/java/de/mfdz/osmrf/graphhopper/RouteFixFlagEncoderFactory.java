package de.mfdz.osmrf.graphhopper;

import com.graphhopper.routing.util.DefaultFlagEncoderFactory;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.PMap;

public class RouteFixFlagEncoderFactory extends DefaultFlagEncoderFactory {

    public FlagEncoder createFlagEncoder(String name, PMap configuration) {
        if (name.equals("bus")) {
            return new BusFlagEncoder(configuration);
        } else {
            return super.createFlagEncoder(name, configuration);
        }
    }
}
