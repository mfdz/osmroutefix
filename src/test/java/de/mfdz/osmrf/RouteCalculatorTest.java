package de.mfdz.osmrf;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.graphhopper.RouteFixHopper;
import de.mfdz.osmrf.osmapi.RelationWrapper;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.data.Relation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
public class RouteCalculatorTest {

    private RouteCalculator routeCalculator;
    private OsmConnection osm = mock(OsmConnection.class);
    private RouteFixHopper hopper = mock(RouteFixHopper.class);

    @Before
    public void setUp() {
        routeCalculator = new RouteCalculator(osm, hopper);
    }

    @Test
    public void testNewRoute_detour() {
        LocationIndex locationIndex =  mock(LocationIndex.class);
        Mockito.when(hopper.getLocationIndex()).thenReturn(locationIndex);
        QueryResult anyResult = new QueryResult(0, 0);
        anyResult.setClosestNode(1);
        Mockito.when(locationIndex.findClosest(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.any(
                EdgeFilter.class))).thenReturn(anyResult);

        Relation detour = routeCalculator.newRoute("detour",
                Arrays.asList(new GHPoint(48.6466475, 8.9412407), new GHPoint(48.653098, 8.934417),
                        new GHPoint(48.6321145, 8.9146409)));

        assertThat(detour.isNew(), is(true));
        assertThat(detour.getTags().get("route"), is("detour"));

        RelationWrapper wrappedRoute = new RelationWrapper(detour);
        assertThat("No start member", wrappedRoute.getFirstMemberWithRole("start"), notNullValue());
        assertThat("No viaNode member", wrappedRoute.getFirstMemberWithRole("viaNode"), notNullValue());
        assertThat("No end member", wrappedRoute.getFirstMemberWithRole("end"), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewRoute_missing() {
        Relation detour = routeCalculator.newRoute("missing",
                Arrays.asList(new GHPoint(48.6466475, 8.9412407), new GHPoint(48.653098, 8.934417),
                        new GHPoint(48.6321145, 8.9146409)));
    }



}