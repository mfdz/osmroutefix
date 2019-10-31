package de.mfdz.osmrf.role;

import com.graphhopper.util.EdgeIteratorState;

public class ForwardBackwardRole implements RoleStrategy {
    public String getRole(EdgeIteratorState edge) {
        return edge.get(EdgeIteratorState.REVERSE_STATE) ? "backward" : "forward";
    }
}
