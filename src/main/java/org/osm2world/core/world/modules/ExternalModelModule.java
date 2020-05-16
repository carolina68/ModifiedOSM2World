package org.osm2world.core.world.modules;

import static java.util.Collections.emptyList;

import org.apache.commons.configuration.Configuration;
import org.osm2world.core.map_data.data.MapArea;
import org.osm2world.core.map_data.data.MapData;
import org.osm2world.core.map_data.data.MapElement;
import org.osm2world.core.map_data.data.MapNode;
import org.osm2world.core.map_data.data.MapWaySegment;
import org.osm2world.core.map_elevation.creation.EleConstraintEnforcer;
import org.osm2world.core.map_elevation.data.EleConnector;
import org.osm2world.core.map_elevation.data.GroundState;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.world.creation.WorldModule;
import org.osm2world.core.world.data.AreaWorldObject;
import org.osm2world.core.world.data.NodeWorldObject;
import org.osm2world.core.world.data.WaySegmentWorldObject;

public class ExternalModelModule implements WorldModule {

	@Override
	public void applyTo(MapData mapData) {

		for (MapElement element : mapData.getMapElements()) {
			if (element.getPrimaryRepresentation() == null
					&& element.getTags().containsKey("3dmr")) {

				if (element instanceof MapNode) {
					((MapNode)element).addRepresentation(new NodePlaceholder((MapNode) element));
				} else if (element instanceof MapWaySegment) {
					((MapWaySegment)element).addRepresentation(new WayPlaceholder((MapWaySegment) element));
				} else if (element instanceof MapArea) {
					((MapArea)element).addRepresentation(new AreaPlaceholder((MapArea) element));
				}

			}
		}

	}

	@Override
	public void setConfiguration(Configuration config) {}

	/** temporary placeholder, to be replaced with an actual 3dmr model by the Target */
	private abstract static class ExternalModelPlaceholder<T extends MapElement> {

		protected final T primaryMapElement;

		protected ExternalModelPlaceholder(T primaryMapElement) {
			this.primaryMapElement = primaryMapElement;
		}

		public void setConfiguration(Configuration config) {}

		public GroundState getGroundState() {
			return GroundState.ON;
		}

		public Iterable<EleConnector> getEleConnectors() {
			return emptyList();
		}

		public void defineEleConstraints(EleConstraintEnforcer enforcer) {}

		public T getPrimaryMapElement() {
			return primaryMapElement;
		}

	}

	private static class NodePlaceholder extends ExternalModelPlaceholder<MapNode> implements NodeWorldObject {

		public NodePlaceholder(MapNode primaryMapElement) {
			super(primaryMapElement);
		}

	}

	private static class WayPlaceholder extends ExternalModelPlaceholder<MapWaySegment> implements WaySegmentWorldObject {

		public WayPlaceholder(MapWaySegment primaryMapElement) {
			super(primaryMapElement);
		}

		@Override public VectorXZ getStartPosition() {
			return primaryMapElement.getStartNode().getPos();
		}

		@Override public VectorXZ getEndPosition() {
			return primaryMapElement.getEndNode().getPos();
		}

	}

	private static class AreaPlaceholder extends ExternalModelPlaceholder<MapArea> implements AreaWorldObject {

		public AreaPlaceholder(MapArea primaryMapElement) {
			super(primaryMapElement);
		}

	}


}
