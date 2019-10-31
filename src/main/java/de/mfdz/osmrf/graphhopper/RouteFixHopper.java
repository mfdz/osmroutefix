/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper licenses this file to you under the Apache License,
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

import com.graphhopper.coll.LongIntMap;
import com.graphhopper.json.geo.JsonFeatureCollection;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.BitUtil;

/**
 *
 * @author Peter Karich (See: )
 * @author Holger Bruch
 */
public class RouteFixHopper extends GraphHopperOSM {

    // mapping of internal edge ID to OSM way ID
    private DataAccess edgeMapping;
    // mapping of internal node ID to OSM node ID
    private DataAccess nodeMapping;
    private BitUtil bitUtil;

    public RouteFixHopper() {}

    public RouteFixHopper(JsonFeatureCollection landmarkSplittingFeatureCollection) {
        super(landmarkSplittingFeatureCollection);
    }

    @Override
    public boolean load(String graphHopperFolder) {
        setFlagEncoderFactory(new RouteFixFlagEncoderFactory());
        //setEncodingManager(EncodingManager.create(new RouteFixFlagEncoderFactory(), "car|turn_costs=true,bus|turn_costs=true", 4));
        boolean loaded = super.load(graphHopperFolder);

        Directory dir = getGraphHopperStorage().getDirectory();
        bitUtil = BitUtil.get(dir.getByteOrder());
        edgeMapping = dir.find("edge_mapping");
        nodeMapping = dir.find("node_mapping");

        if (loaded) {
            edgeMapping.loadExisting();
            nodeMapping.loadExisting();
        }

        return loaded;
    }

    @Override
    protected DataReader createReader(GraphHopperStorage ghStorage) {
        OSMReader reader = new OSMReader(ghStorage) {

            {
                edgeMapping.create(1000);
                nodeMapping.create(1000);
            }

            // this method is only in >0.6 protected, before it was private
            @Override
            protected void storeOsmWayID(int edgeId, long osmWayId) {
                super.storeOsmWayID(edgeId, osmWayId);

                long pointer = 8L * edgeId;
                edgeMapping.ensureCapacity(pointer + 8L);

                edgeMapping.setInt(pointer, bitUtil.getIntLow(osmWayId));
                edgeMapping.setInt(pointer + 4, bitUtil.getIntHigh(osmWayId));
            }

            // this method is not hacked in a hacked OSMReader
            @Override
            protected void storeOsmNodeID(int nodeId, long osmNodeId) {
                super.storeOsmNodeID(nodeId, osmNodeId);
                //if (nodeId<0)
                //    System.out.println(nodeId+ " "+osmNodeId);
                long pointer = 8L * nodeId;
                nodeMapping.ensureCapacity(pointer + 8L);

                nodeMapping.setInt(pointer, bitUtil.getIntLow(osmNodeId));
                nodeMapping.setInt(pointer + 4, bitUtil.getIntHigh(osmNodeId));
            }

            @Override
            protected void finishedReading() {
                super.finishedReading();

                edgeMapping.flush();
                nodeMapping.flush();
            }
        };

        return initDataReader(reader);
    }

    public long getOSMWay(int internalEdgeId) {
        long pointer = 8L * internalEdgeId;
        return bitUtil.combineIntsToLong(edgeMapping.getInt(pointer), edgeMapping.getInt(pointer + 4L));
    }

    public long getOSMNode(int internalNodeId) {
        long pointer = 8L * internalNodeId;
        return bitUtil.combineIntsToLong(nodeMapping.getInt(pointer), nodeMapping.getInt(pointer + 4L));
    }
}
