/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.mfdz.osmrf.graphhopper;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.MultiException;
import com.graphhopper.http.WebHelper;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.util.Constants;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.gpx.GpxFromInstructions;
import com.graphhopper.util.shapes.GHPoint;
import de.mfdz.osmrf.RouteCalculator;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.westnordost.osmapi.OsmConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.graphhopper.util.Parameters.Details.PATH_DETAILS;
import static com.graphhopper.util.Parameters.Routing.*;

/**
 * Resource to return route application like mobile or browser. Note: If type
 * is json it returns the points in GeoJson array format [longitude,latitude] unlike the format "lat,lon"
 * used for the request. See the full API response format in docs/web/api-doc.md
 *
 * @author Peter Karich
 */
@Path("osmRouteRelation")
public class RouteForOsmRouteResource {

    private static final Logger logger = LoggerFactory.getLogger(RouteForOsmRouteResource.class);

    private final GraphHopperAPI graphHopper;
    private final Boolean hasElevation;

    @Inject
    public RouteForOsmRouteResource(GraphHopperAPI graphHopper, @Named("hasElevation") Boolean hasElevation) {
        this.graphHopper = graphHopper;
        this.hasElevation = hasElevation;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/gpx+xml"})
    public Response doGet(
            @Context HttpServletRequest httpReq,
            @Context UriInfo uriInfo,
            @Context ContainerRequestContext rc,
            @QueryParam(WAY_POINT_MAX_DISTANCE) @DefaultValue("1") double minPathPrecision,
            @QueryParam("point") List<GHPoint> requestPoints,
            @QueryParam("type") @DefaultValue("json") String type,
            @QueryParam(INSTRUCTIONS) @DefaultValue("true") boolean instructions,
            @QueryParam(CALC_POINTS) @DefaultValue("true") boolean calcPoints,
            @QueryParam("elevation") @DefaultValue("false") boolean enableElevation,
            @QueryParam("points_encoded") @DefaultValue("true") boolean pointsEncoded,
            @QueryParam("routeId") Long routeId,
            @QueryParam("routeType") String routeType,
            @QueryParam("locale") @DefaultValue("en") String localeStr,
            @QueryParam(PATH_DETAILS) List<String> pathDetails,
            @QueryParam("gpx.route") @DefaultValue("true") boolean withRoute /* default to false for the route part in next API version, see #437 */,
            @QueryParam("gpx.track") @DefaultValue("true") boolean withTrack,
            @QueryParam("gpx.waypoints") @DefaultValue("false") boolean withWayPoints,
            @QueryParam("gpx.trackname") @DefaultValue("GraphHopper Track") String trackName,
            @QueryParam("gpx.millis") String timeString) {
        boolean writeGPX = "gpx".equalsIgnoreCase(type);
        instructions = writeGPX || instructions;

        StopWatch sw = new StopWatch().start();

        if ((requestPoints.isEmpty() || null == routeType) && null == routeId)
            throw new IllegalArgumentException("You have to pass either an Id of an existing OSM route relation or at least one point and a route type (e.g. detour or bus)");
        if (enableElevation && !hasElevation)
            throw new IllegalArgumentException("Elevation not supported!");

        RouteCalculator routeCalculator = getRouteCalculator();
        GHRequest request = null;
        if (routeId != null) {
            CachedRelationWrapper routeRelation = routeCalculator.getRouteRelation(routeId);
            request = routeCalculator.existingRouteRequest(routeRelation);
        } else {
            request = routeCalculator
                    .newRouteRequest(routeType, requestPoints);
        }
        request.setLocale(localeStr).
                setPathDetails(pathDetails).
                getHints().
                put(CALC_POINTS, calcPoints).
                put(INSTRUCTIONS, instructions).
                put(WAY_POINT_MAX_DISTANCE, minPathPrecision);

        GHResponse ghResponse = graphHopper.route(request);

        // TODO: Request logging and timing should perhaps be done somewhere outside
        float took = sw.stop().getSeconds();
        String infoStr = httpReq.getRemoteAddr() + " " + httpReq.getLocale() + " " + httpReq.getHeader("User-Agent");
        String logStr = httpReq.getQueryString() + " " + infoStr + " " + requestPoints + ", took:"
                + took + ", routeType: " + routeType + ", routeId: " + routeId;

        if (ghResponse.hasErrors()) {
            logger.error(logStr + ", errors:" + ghResponse.getErrors());
            throw new MultiException(ghResponse.getErrors());
        } else {
            logger.info(logStr + ", alternatives: " + ghResponse.getAll().size()
                    + ", distance0: " + ghResponse.getBest().getDistance()
                    + ", weight0: " + ghResponse.getBest().getRouteWeight()
                    + ", time0: " + Math.round(ghResponse.getBest().getTime() / 60000f) + "min"
                    + ", points0: " + ghResponse.getBest().getPoints().getSize()
                    + ", debugInfo: " + ghResponse.getDebugInfo());
            return writeGPX ?
                    gpxSuccessResponseBuilder(ghResponse, timeString, trackName, enableElevation, withRoute, withTrack, withWayPoints, Constants.VERSION).
                            header("X-GH-Took", "" + Math.round(took * 1000)).
                            build()
                    :
                    Response.ok(WebHelper.jsonObject(ghResponse, instructions, calcPoints, enableElevation, pointsEncoded, took)).
                            header("X-GH-Took", "" + Math.round(took * 1000)).
                            build();
        }
    }

    private RouteCalculator getRouteCalculator() {
        OsmConnection osm = new OsmConnection(
                "https://api.openstreetmap.org/api/0.6/",
                "OSMRouteFix", null);
        return new RouteCalculator(osm,
                (RouteFixHopper) graphHopper);
    }

    private static Response.ResponseBuilder gpxSuccessResponseBuilder(GHResponse ghRsp, String timeString, String
            trackName, boolean enableElevation, boolean withRoute, boolean withTrack, boolean withWayPoints, String version) {
        if (ghRsp.getAll().size() > 1) {
            throw new IllegalArgumentException("Alternatives are currently not yet supported for GPX");
        }

        long time = timeString != null ? Long.parseLong(timeString) : System.currentTimeMillis();
        InstructionList instructions = ghRsp.getBest().getInstructions();
        return Response.ok(GpxFromInstructions.createGPX(instructions, trackName, time, enableElevation, withRoute, withTrack, withWayPoints, version, instructions.getTr()), "application/gpx+xml").
                header("Content-Disposition", "attachment;filename=" + "GraphHopper.gpx");
    }
}
