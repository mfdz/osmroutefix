package de.mfdz.osmrf.osmapi;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.map.data.*;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

/** Writes elements into a Level0L format */
public class MapDataChangesLevel0LWriter implements ApiRequestWriter, StreamingOutput
{
	private long changesetId;
	private List<Element> creations;
	private List<Element> modifications;
	private List<Element> deletions;
	private StringBuilder out = new StringBuilder();


	public MapDataChangesLevel0LWriter(long changesetId, Iterable<Element> elements) {
		this.changesetId = changesetId;

		creations = new ArrayList<>();
		modifications = new ArrayList<>();
		deletions = new ArrayList<>();
		for(Element element : elements)
		{
			// new deleted elements are ignored
			if(element.isNew() && element.isDeleted())
				continue;
			else if(element.isNew())
				creations.add(element);
			else if(element.isDeleted())
				deletions.add(element);
			else if(element.isModified())
				modifications.add(element);
		}
		/* Order changes in such a way that they can be applied to a data store while maintaining
		   data integrity (ie. a database). For example, the ordering prevents a way being added
		   before the underlying nodes are created.
		   Idea taken from Osmosis.*/
		Collections.sort(creations, new OrderByNodeWayRelation());
		Collections.sort(modifications, new OrderByRelationWayNode());
		Collections.sort(deletions, new OrderByRelationWayNode());
	}

	public boolean hasChanges() {
		return !creations.isEmpty() || !modifications.isEmpty() || !deletions.isEmpty();
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		write();
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(out.toString());
		outputStreamWriter.flush();
	}

	private class OrderByRelationWayNode implements Comparator<Element> {
		public int compare(Element lhs, Element rhs) {
			return getTypeOrder(rhs.getType()) - getTypeOrder(lhs.getType());
		}
	}

	private class OrderByNodeWayRelation implements Comparator<Element> {
		public int compare(Element lhs, Element rhs) {
			return getTypeOrder(lhs.getType()) - getTypeOrder(rhs.getType());
		}
	}

	private static int getTypeOrder(Element.Type type) {
		switch(type) {
			case NODE:		return 1;
			case WAY:		return 2;
			case RELATION:	return 3;
		}
		return 0;
	}

	protected void write() throws IOException {
		if(!creations.isEmpty()) {
			for (Element element : creations) writeElement(element);
		}

		if(!modifications.isEmpty()) {
			for (Element element : modifications) writeElement(element);
		}

		if(!deletions.isEmpty()) {
			for (Element element : deletions) writeDeleteElement(element);
		}
	}

	private void writeDeleteElement(Element element) {
		out.append(String.format("-{} {}\n", toLevel0LType(element.getType()), element.getId()));
	}

	private void writeElement(Element element) throws IOException {
		if(element instanceof Node) {
			writeNode((Node) element);
		} else if(element instanceof Way) {
			writeWay((Way) element);
		} else if(element instanceof Relation) {
			writeRelation((Relation) element);
		}
		writeTags(element.getTags());
	}

	private static String toLevel0LType(Element.Type type) {
		return type.toString().toLowerCase(Locale.UK);
	}


	private String toLevel0LTypeAbrev(Element.Type type) {
		switch(type) {
		case WAY:
			return "wy";
		case RELATION:
			return "rel";
		case NODE:
		default:
			return "nd";
		}
	}

	private void writeNode(Node node) throws IOException {
		LatLon position = node.getPosition();
		out.append(String.format("node %d: %f %f\n", node.getId(), position.getLatitude(), position.getLongitude()));
	}

	private void writeWay(Way way) {
		out.append(String.format("way %d\n", way.getId()));
		for(Long node : way.getNodeIds()) {
			out.append(String.format("  nd %d\n", node));
		}
	}

	private void writeRelation(Relation relation) {
		out.append(String.format("relation %d\n", relation.getId()));
		for(RelationMember member : relation.getMembers()) {
			out.append(String.format("  %s %d %s\n", toLevel0LTypeAbrev(member.getType()), member.getRef(), member.getRole()));
		}
	}

	private void writeTags(Map<String, String> tags) {
		if(tags != null) {
			for (Map.Entry<String, String> tag : tags.entrySet()) {
				out.append(String.format("  %s = %s\n", tag.getKey().replace("=", "\\="), tag.getValue()));
			}
		}
	}
}