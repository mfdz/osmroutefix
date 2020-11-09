package de.mfdz.osmrf.validation;

import com.google.common.collect.Iterables;
import de.mfdz.osmrf.osmapi.CachedRelationWrapper;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Way;

import java.util.List;

public class ConnectivityValidator {

    public static ComparisonResult validateConnectivity(CachedRelationWrapper rel) {
        ComparisonResult result = new ComparisonResult();
        Way lastWay = null, currentWay;
        Long nextConnectionNode = null, tCrossingNode = null;
        StringBuffer unconnectedWays = new StringBuffer();
        StringBuffer waysToSplit = new StringBuffer();
        for (Element way : rel.getMemberElementsOfType(Element.Type.WAY)) {
            currentWay = (Way) way;
            if (lastWay != null) {
                nextConnectionNode = findNextConnectionNode(lastWay, currentWay);
                if (nextConnectionNode == null) {
                    tCrossingNode = findTCrossingNode(lastWay, currentWay);
                    if (tCrossingNode == null) {
                        unconnectedWays
                                .append("Ways w" + lastWay.getId() + " and w" + currentWay.getId()
                                        + " are not connected at end node. ");
                    } else {
                        if (!"roundabout".equals(currentWay.getTags().get("junction"))
                                && !"roundabout".equals(lastWay.getTags().get("junction"))) {
                            waysToSplit.append("Ways w" + lastWay.getId() + " or w" + currentWay
                                    .getId() + " have a t-crossing. Way should be split at n"
                                    + tCrossingNode + ". ");
                        }
                    }
                }
            }
            lastWay = (Way) way;
        }

        if (unconnectedWays.length() > 0) {
            result.addIssue("unconnectedWays", unconnectedWays.toString());
        }
        if (waysToSplit.length() > 0) {
            result.addIssue("waysToSplit", waysToSplit.toString());
        }
        return result;
    }

    private static Long findNextConnectionNode(Way lastWay, Way currentWay) {
        List<Long> currentWayNodeIds = currentWay.getNodeIds();
        List<Long> lastWayNodeIds = lastWay.getNodeIds();
        Long startNode = currentWayNodeIds.get(0);
        Long endNode = Iterables.getLast(currentWayNodeIds);

        if (startNode.equals(Iterables.getFirst(lastWayNodeIds, null)) || startNode
                .equals(Iterables.getLast(lastWayNodeIds))) {
            return endNode;
        } else if (endNode.equals(Iterables.getFirst(lastWayNodeIds, null)) || endNode
                .equals(Iterables.getLast(lastWayNodeIds))) {
            return startNode;
        }
        return null;
    }

    private static Long findTCrossingNode(Way lastWay, Way currentWay) {
        Long tCrossing = wayReferecesEitherNode(currentWay, lastWay.getNodeIds().get(0),
                Iterables.getLast(lastWay.getNodeIds()));
        return tCrossing != null ?
                tCrossing :
                wayReferecesEitherNode(lastWay, currentWay.getNodeIds().get(0),
                        Iterables.getLast(currentWay.getNodeIds()));
    }

    private static Long wayReferecesEitherNode(Way way, Long startNode, Long endNode) {
        for (Long nodeId : way.getNodeIds()) {
            if (nodeId.equals(startNode)) {
                return startNode;
            } else if (nodeId.equals(endNode)) {
                return endNode;
            }
        }
        return null;
    }
}
