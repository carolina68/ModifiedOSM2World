package org.osm2world.core.map_data.data;

import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.osm2world.core.map_data.data.overlaps.MapOverlap;
import org.osm2world.core.math.datastructures.IntersectionTestObject;
import org.osm2world.core.world.data.WorldObject;

import de.topobyte.osm4j.core.model.iface.OsmEntity;

/**
 * An element from an OSM dataset.
 *
 * @See {@link MapData} for context
 */
public interface MapElement extends IntersectionTestObject {

	/**
	 * returns the visual representations of this element.
	 *
	 * The order should match the order in which they were added,
	 * so that dependencies are preserved (elements that depend on
	 * another element should be placed after that element).
	 * The first element is considered the "primary" representation,
	 * and for some purposes - such as elevation calculation -, only this
	 * representation will be used.
	 */
	public List<? extends WorldObject> getRepresentations();

	/**
	 * returns the primary representation, or null if the object doesn't have any.
	 * @see #getRepresentations()
	 */
	public WorldObject getPrimaryRepresentation();

	/**
	 * returns all overlaps between this {@link MapElement}
	 * and other {@link MapElement}s.
	 */
	public Collection<MapOverlap<? extends MapElement, ? extends MapElement>> getOverlaps();

	/** returns the underlying {@link OsmEntity} */
	OsmEntity getOsmElement();

	/** returns this element's tags */
	TagGroup getTags();

}
