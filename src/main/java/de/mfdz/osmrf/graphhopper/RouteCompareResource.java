package de.mfdz.osmrf.graphhopper;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.exceptions.PointOutOfBoundsException;
import de.mfdz.osmrf.validation.ComparisonResult;
import de.mfdz.osmrf.RouteCalculator;
import de.westnordost.osmapi.OsmConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;

@javax.ws.rs.Path("routeCompare")
public class RouteCompareResource {
    private static final Logger logger = LoggerFactory.getLogger(RouteCompareResource.class);

    private final GraphHopper hopper;
    private final OsmConnection osm;
    private final RouteCalculator routeCalculator;

    @Inject
    public RouteCompareResource(GraphHopper graphHopper) {
        this.hopper = graphHopper;
        this.osm = new OsmConnection(
                "https://api.openstreetmap.org/api/0.6/",
                "OSMRouteFix", null);
        this.routeCalculator = new RouteCalculator(osm, (RouteFixHopper)this.hopper);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet(
            @Context HttpServletRequest httpReq,
            @Context UriInfo uriInfo,
            @QueryParam("routeId") long routeId,
            @QueryParam("node") List<Long> nodes) {
        StopWatch sw = new StopWatch().start();
        float took = 0;
        HashMap<String, String> result = new HashMap<>();

        try {
            ComparisonResult comparisonResult = routeCalculator.compareRoutes(routeId, nodes);

            took = sw.stop().getSeconds();
            logger.debug(String.format("RouteCompare {}", routeId) + ", took:" + took);

            return Response.ok().entity(comparisonResult).build();
        } catch (PointOutOfBoundsException poobx) {
            result.put("error", "Server bounding box does not include specified coordinate");
            return Response.status(422).entity(result).build();
        }
    }
}
