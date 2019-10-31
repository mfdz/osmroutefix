package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.MapDataHandler;

import java.util.HashMap;

public final class CachingMapDataHandler implements MapDataHandler {

    HashMap<Long, Node> nodes = new HashMap<>();
    HashMap<Long, Way> ways = new HashMap<>();
    HashMap<Long, Relation> relations = new HashMap<>();

    @Override
    public void handle(BoundingBox boundingBox) {
    }

    @Override
    public void handle(Node node) {
        nodes.put(node.getId(), node);
    }

    @Override
    public void handle(Way way) {
        ways.put(way.getId(), way);
    }

    @Override
    public void handle(Relation relation) {
        relations.put(relation.getId(), relation);
    }

    public Node getNode(long id) {
        return nodes.get(id);
    }

    public Way getWay(long id) {
        return ways.get(id);
    }

    public OsmRelation getRelation(long id) {
        return (OsmRelation) relations.get(id);
    }
}
