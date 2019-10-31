package de.mfdz.osmrf.graphhopper;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.TranslationMap;
import de.mfdz.osmrf.RouteCalculator;
import de.mfdz.osmrf.osmapi.MapDataChangesLevel0LWriter;
import de.mfdz.osmrf.osmapi.StreamingMapDataChangesWriter;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.changes.MapDataChangesWriter;
import de.westnordost.osmapi.map.data.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

@javax.ws.rs.Path("routeFix")
public class RouteFixResource {
    private static final Logger logger = LoggerFactory.getLogger(RouteFixResource.class);

    private final GraphHopper hopper;
    private final OsmConnection osm;
    private final RouteCalculator routeCalculator;

    @Inject
    public RouteFixResource(GraphHopper graphHopper) {
        this.hopper = graphHopper;
        this.osm = new OsmConnection(
                "https://api.openstreetmap.org/api/0.6/",
                "OSMRouteFix", null);
        this.routeCalculator = new RouteCalculator(osm, (RouteFixHopper)this.hopper);
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML, "text/level0l"})
    public Response doGet(
            @Context HttpServletRequest httpReq,
            @Context UriInfo uriInfo,
            @QueryParam("routeId") long routeId,
            @QueryParam("type") @DefaultValue("level0l") String outType) {
        StopWatch sw = new StopWatch().start();
        float took = 0;

        Relation r = routeCalculator.getUpdatedRoute(
                //2068306
                //35496
                routeId
        );

        took = sw.stop().getSeconds();
        logger.debug(String.format("RouteFix {}", routeId) + ", took:" + took);
        if ("xml".equals(outType)) {
            return Response.ok(new StreamingMapDataChangesWriter(1, Arrays.asList(r)), MediaType.APPLICATION_XML).
                    header("X-GH-Took", "" + Math.round(took * 1000)).
                    build();
        } else {
            return Response.ok(new MapDataChangesLevel0LWriter(1, Arrays.asList(r)), "text/plain; charset=utf-8").
                    encoding("UTF-8").
                    header("X-GH-Took", "" + Math.round(took * 1000)).
                    build();
        }
    }
}
