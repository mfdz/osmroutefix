package de.mfdz.osmrf.role;

import com.graphhopper.util.EdgeIteratorState;

public interface RoleStrategy {

    String getRole(EdgeIteratorState edge);
}
