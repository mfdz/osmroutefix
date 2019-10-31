//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.mfdz.osmrf.graphhopper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GraphHopper;
import com.graphhopper.json.geo.JsonFeatureCollection;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.util.spatialrules.SpatialRuleLookupHelper;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;
import io.dropwizard.lifecycle.Managed;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteFixManaged implements Managed {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GraphHopper graphHopper;

    public RouteFixManaged(CmdArgs configuration, ObjectMapper objectMapper) {
        ObjectMapper localObjectMapper = objectMapper.copy();
        localObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String splitAreaLocation = configuration.get("prepare.lm.split_area_location", "");

        JsonFeatureCollection landmarkSplittingFeatureCollection;
        try {
            Reader reader = splitAreaLocation.isEmpty() ? new InputStreamReader(LandmarkStorage.class.getResource("map.geo.json").openStream(), Helper.UTF_CS) : new InputStreamReader(new FileInputStream(splitAreaLocation), Helper.UTF_CS);
            Throwable var7 = null;

            try {
                landmarkSplittingFeatureCollection = (JsonFeatureCollection)localObjectMapper.readValue(reader, JsonFeatureCollection.class);
            } catch (Throwable var35) {
                var7 = var35;
                throw var35;
            } finally {
                if (reader != null) {
                    if (var7 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var34) {
                            var7.addSuppressed(var34);
                        }
                    } else {
                        reader.close();
                    }
                }

            }
        } catch (IOException var39) {
            this.logger.error("Problem while reading border map GeoJSON. Skipping this.", var39);
            landmarkSplittingFeatureCollection = null;
        }

        this.graphHopper = createGraphHopper(landmarkSplittingFeatureCollection).forServer();
        String spatialRuleLocation = configuration.get("spatial_rules.location", "");
        if (!spatialRuleLocation.isEmpty()) {
            BBox maxBounds = BBox.parseBBoxString(configuration.get("spatial_rules.max_bbox", "-180, 180, -90, 90"));

            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(spatialRuleLocation), Helper.UTF_CS);
                Throwable var9 = null;

                try {
                    JsonFeatureCollection jsonFeatureCollection = (JsonFeatureCollection)localObjectMapper.readValue(reader, JsonFeatureCollection.class);
                    SpatialRuleLookupHelper.buildAndInjectSpatialRuleIntoGH(this.graphHopper, maxBounds, jsonFeatureCollection);
                } catch (Throwable var33) {
                    var9 = var33;
                    throw var33;
                } finally {
                    if (reader != null) {
                        if (var9 != null) {
                            try {
                                reader.close();
                            } catch (Throwable var32) {
                                var9.addSuppressed(var32);
                            }
                        } else {
                            reader.close();
                        }
                    }

                }
            } catch (IOException var37) {
                throw new RuntimeException(var37);
            }
        }

        this.graphHopper.init(configuration);
    }

    private RouteFixHopper createGraphHopper(
            JsonFeatureCollection landmarkSplittingFeatureCollection) {
        RouteFixHopper hopper = new RouteFixHopper(landmarkSplittingFeatureCollection);
        hopper.setFlagEncoderFactory(new RouteFixFlagEncoderFactory());
        return hopper;
    }

    public void start() {
        this.graphHopper.importOrLoad();
        this.logger.info("loaded graph at:" + this.graphHopper.getGraphHopperLocation() + ", data_reader_file:" + this.graphHopper.getDataReaderFile() + ", encoded values:" + this.graphHopper.getEncodingManager().toEncodedValuesAsString() + ", " + this.graphHopper.getGraphHopperStorage().toDetailsString());
    }

    GraphHopper getGraphHopper() {
        return this.graphHopper;
    }

    public void stop() {
        this.graphHopper.close();
    }
}
