package org.osm2world.core.test;

import static java.util.Collections.*;
import static org.osm2world.core.target.common.material.Materials.PLASTIC;

import org.osm2world.core.map_data.data.MapNode;
import org.osm2world.core.map_elevation.data.GroundState;
import org.osm2world.core.math.TriangleXYZ;
import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.target.RenderableToAllTargets;
import org.osm2world.core.target.Target;
import org.osm2world.core.target.frontend_pbf.FrontendPbf.WorldObject;
import org.osm2world.core.world.data.NoOutlineNodeWorldObject;
import org.osm2world.core.world.modules.common.AbstractModule;

/**
 * a world module for unit tests that produces simple and predictable {@link WorldObject}s
 */
public class TestWorldModule extends AbstractModule {

	@Override
	protected void applyToNode(MapNode node) {
		node.addRepresentation(new TestNodeWorldObject(node));
	}

	public static class TestNodeWorldObject extends NoOutlineNodeWorldObject implements RenderableToAllTargets {

		public TestNodeWorldObject(MapNode node) {
			super(node);
		}

		@Override
		public GroundState getGroundState() {
			return GroundState.ON;
		}

		@Override
		public void renderTo(Target<?> target) {

			VectorXYZ base = node.getPos().xyz(0);

			TriangleXYZ triangle = new TriangleXYZ(base, base.add(0, 1, 0), base.add(1, 1, 0));
			target.drawTriangles(PLASTIC, singletonList(triangle), emptyList());

		}

	}

}
