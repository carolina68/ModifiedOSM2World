package org.osm2world.core.map_data.data;

import static de.topobyte.osm4j.core.model.util.OsmModelUtil.getTagsAsMap;
import static java.util.Comparator.comparingDouble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.osm2world.core.map_data.data.overlaps.MapOverlap;
import org.osm2world.core.math.AxisAlignedBoundingBoxXZ;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.world.data.NodeWorldObject;

import de.topobyte.osm4j.core.model.iface.OsmNode;


/**
 * A node from an OSM dataset.
 *
 * @See {@link MapData} for context
 */
public class MapNode extends MapRelation.Element implements MapElement {

	private final VectorXZ pos;
	private final OsmNode osmNode;

	private List<NodeWorldObject> representations = new ArrayList<NodeWorldObject>(1);

	private List<MapWaySegment> connectedWaySegments = new ArrayList<MapWaySegment>();
	private List<MapSegment> connectedSegments = new ArrayList<MapSegment>();

	private List<MapWaySegment> inboundLines = new ArrayList<MapWaySegment>(); //TODO: maybe use list and sort by angle?
	private List<MapWaySegment> outboundLines = new ArrayList<MapWaySegment>();

	private Collection<MapArea> adjacentAreas = new ArrayList<MapArea>();

	public MapNode(VectorXZ pos, OsmNode osmNode) {
		this.pos = pos;
		this.osmNode = osmNode;
	}

	public VectorXZ getPos() {
		return pos;
	}

	@Override
	public OsmNode getOsmElement() {
		return osmNode;
	}

	@Override
	public TagGroup getTags() {
		return new MapBasedTagGroup(getTagsAsMap(osmNode));
	}

	public Collection<MapArea> getAdjacentAreas() {
		return adjacentAreas;
	}

	public void addInboundLine(MapWaySegment inboundLine) {

		connectedWaySegments.add(inboundLine);
		connectedSegments.add(inboundLine);
		inboundLines.add(inboundLine);

		sortLinesByAngle(connectedWaySegments);
		sortLinesByAngle(connectedSegments);
		sortLinesByAngle(inboundLines);

	}

	public void addOutboundLine(MapWaySegment outboundLine) {

		connectedWaySegments.add(outboundLine);
		connectedSegments.add(outboundLine);
		outboundLines.add(outboundLine);

		sortLinesByAngle(connectedWaySegments);
		sortLinesByAngle(connectedSegments);
		sortLinesByAngle(outboundLines);

	}

	/**
	 * returns those connected lines that end here.
	 * Sorting is as for {@link #getConnectedWaySegments()}.
	 */
	public List<MapWaySegment> getInboundLines() {
		return inboundLines;
	}

	/**
	 * returns those connected lines that start here.
	 * Sorting is as for {@link #getConnectedWaySegments()}.
	 */
	public List<MapWaySegment> getOutboundLines() {
		return outboundLines;
	}

	public void addAdjacentArea(MapArea adjacentArea) {
		if (!adjacentAreas.contains(adjacentArea)) {
			adjacentAreas.add(adjacentArea);
		}
	}

	//TODO: with all that "needs to be called before x" etc. stuff (also in MapArea), switch to BUILDER?
	/** needs to be called after adding and completing all adjacent areas */
	public void calculateAdjacentAreaSegments() {

		for (MapArea adjacentArea : adjacentAreas) {
			for (MapAreaSegment areaSegment : adjacentArea.getAreaSegments()) {
				if (areaSegment.getStartNode() == this
						|| areaSegment.getEndNode() == this) {
					connectedSegments.add(areaSegment);
				}
			}
		}

		sortLinesByAngle(connectedSegments);
	}

	public Collection<MapWay> getConnectedWays() {

		List<MapWay> result = new ArrayList<>();

		for (MapWaySegment segment : connectedWaySegments) {
			if (!result.contains(segment.getWay())) {
				result.add(segment.getWay());
			}
		}

		return result;

	}

	/**
	 * returns all way segments connected with this node.
	 * They will be sorted according to the clockwise
	 * (seen from above) angle between the vector
	 * "this node -> other node of the segment"
	 * and the positive x direction.
	 */
	public List<MapWaySegment> getConnectedWaySegments() {
		return connectedWaySegments;
	}

	/**
	 * returns all way segments and area segments connected with this node.
	 * Sorted like {@link #getConnectedWaySegments()}.
	 */
	public List<MapSegment> getConnectedSegments() {
		return connectedSegments;
	}

	/**
	 * creates the ordering described for {@link #getConnectedSegments()}
	 */
	private void sortLinesByAngle(List<? extends MapSegment> lines) {

		lines.sort(comparingDouble((MapSegment l) -> {

			VectorXZ d = l.getDirection();

			if (inboundLines.contains(l)) {
				d = d.invert();
			}

			return d.angle();

		}));

	}

	@Override
	public List<NodeWorldObject> getRepresentations() {
		return representations;
	}

	@Override
	public NodeWorldObject getPrimaryRepresentation() {
		if (representations.isEmpty()) {
			return null;
		} else {
			return representations.get(0);
		}
	}

	/**
	 * adds a visual representation for this node
	 */
	public void addRepresentation(NodeWorldObject representation) {
		this.representations.add(representation);
	}

	@Override
	public String toString() {
		return "n" + osmNode.getId();
	}

	@Override
	public Collection<MapOverlap<?,?>> getOverlaps() {
		return Collections.emptyList();
	}

	@Override
	public AxisAlignedBoundingBoxXZ getAxisAlignedBoundingBoxXZ() {
		return new AxisAlignedBoundingBoxXZ(pos.x, pos.z, pos.x, pos.z);
	}

}
