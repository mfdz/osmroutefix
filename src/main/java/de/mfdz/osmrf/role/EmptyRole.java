package de.mfdz.osmrf.role;

import com.graphhopper.util.EdgeIteratorState;

public class EmptyRole implements RoleStrategy {
    public String getRole(EdgeIteratorState edge) {
        return "";
    }
}
