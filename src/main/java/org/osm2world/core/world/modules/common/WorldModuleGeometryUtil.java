package org.osm2world.core.world.modules.common;

import static java.lang.Math.toRadians;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.osm2world.core.map_elevation.data.GroundState;
import org.osm2world.core.math.GeometryUtil;
import org.osm2world.core.math.InvalidGeometryException;
import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.math.shapes.PolygonShapeXZ;
import org.osm2world.core.world.creation.WorldModule;
import org.osm2world.core.world.data.WorldObject;
import org.osm2world.core.world.data.WorldObjectWithOutline;

/**
 * offers some geometry-related utility functions for {@link WorldModule}s
 */
public final class WorldModuleGeometryUtil {

	private WorldModuleGeometryUtil() { }

	/**
	 * creates the vectors for a vertical triangle strip
	 * at a given elevation above a line of points
	 */
	public static final List<VectorXYZ> createVerticalTriangleStrip(
			List<? extends VectorXYZ> baseLine, float stripLowerYBound, float stripUpperYBound) {

		VectorXYZ[] result = new VectorXYZ[baseLine.size() * 2];

		for (int i = 0; i < baseLine.size(); i++) {

			VectorXYZ basePos = baseLine.get(i);

			result[i*2] = new VectorXYZ(
					basePos.getX(),
					basePos.getY() + stripLowerYBound,
					basePos.getZ());

			result[i*2+1] = new VectorXYZ(
					basePos.getX(),
					basePos.getY() + stripUpperYBound,
					basePos.getZ());

		}

		return asList(result);

	}

	/**
	 * creates a triangle strip between two outlines with identical number of vectors
	 */
	public static final List<VectorXYZ> createTriangleStripBetween(
			List<VectorXYZ> leftOutline, List<VectorXYZ> rightOutline) {

		assert leftOutline.size() == rightOutline.size();

		VectorXYZ[] vs = new VectorXYZ[leftOutline.size() * 2];

		for (int i = 0; i < leftOutline.size(); i++) {
			vs[i*2] = leftOutline.get(i);
			vs[i*2+1] = rightOutline.get(i);
		}

		return asList(vs);

	}


	/**
	 * @param ratio  0 is at left outline, 1 at right outline
	 */
	public static final List<VectorXYZ> createLineBetween(
			List<VectorXYZ> leftOutline, List<VectorXYZ> rightOutline, float ratio) {

		assert leftOutline.size() == rightOutline.size();

		List<VectorXYZ> result = new ArrayList<VectorXYZ>(leftOutline.size());

		for (int i = 0; i < leftOutline.size(); i++) {
			result.add(GeometryUtil.interpolateBetween(
					leftOutline.get(i), rightOutline.get(i), ratio));
		}

		return result;

	}

	/**
	 * creates an rotated version of a list of vectors
	 * by rotating them by the given angle around the parallel of the x axis
	 * defined by the given Y and Z coordinates
	 *
	 * @param angle  rotation angle in degrees
	 */
	public static final List<VectorXYZ> rotateShapeX(List<VectorXYZ> shape,
			double angle, double posY, double posZ) {

		VectorXYZ[] result = new VectorXYZ[shape.size()];

		for (int i = 0; i < shape.size(); ++i) {
			result[i] = shape.get(i).add(0f, -posY, -posZ);
			result[i] = result[i].rotateX(toRadians(angle));
			result[i] = result[i].add(0f, posY, posZ);
		}

		return asList(result);

	}

	/**
	 * moves a shape that was defined at the origin to a new position.
	 *
	 * For reference: The identity transformation is obtained by setting center
	 * to {@link VectorXYZ#NULL_VECTOR}, forward to {@link VectorXYZ#Z_UNIT},
	 * and up to {@link VectorXYZ#Y_UNIT}.
	 *
	 * @param center   new center coordinate
	 * @param forward  new forward direction (unit vector)
	 * @param up       new up direction (unit vector)
	 * @return         list of 3d vectors; same length as shape
	 */
	public static final List<VectorXYZ> transformShape(List<VectorXYZ> shape,
			VectorXYZ center, VectorXYZ forward, VectorXYZ up) {

		VectorXYZ[] result = new VectorXYZ[shape.size()];

		VectorXYZ right = up.cross(forward).normalize();

		final double[][] m = { //rotation matrix
				{right.x,   right.y,   right.z},
				{up.x,      up.y,      up.z},
				{forward.x, forward.y, forward.z}
		};

		for (int i = 0; i < shape.size(); i++) {

			VectorXYZ v = shape.get(i);

			v = new VectorXYZ(
					m[0][0] * v.x + m[1][0] * v.y + m[2][0] * v.z,
					m[0][1] * v.x + m[1][1] * v.y + m[2][1] * v.z,
					m[0][2] * v.x + m[1][2] * v.y + m[2][2] * v.z
					);

			v = v.add(center);

			result[i] = v;

		}

		return asList(result);

	}


	/**
	 * removes positions from a collection if they are on the area covered by a
	 * {@link WorldObjectWithOutline} from a collection of {@link WorldObject}s.
	 *
	 * This can be used to avoid placing trees, bridge pillars
	 * and other randomly distributed features on roads, rails
	 * or other similar places where they don't belong.
	 */
	public static final void filterWorldObjectCollisions(
			Collection<VectorXZ> positions,
			Collection<WorldObject> worldObjects) {

		//TODO: add support for avoiding a radius around the position, too.
		//this is easily possible once "inflating"/"shrinking" polygons is supported [would also be useful for water bodies etc.]

		/*
		 * prepare filter polygons.
		 * It improves performance to construct the outline polygons only once
		 * instead of doing this within the loop iterating over positions.
		 */

		List<PolygonShapeXZ> filterPolygons = new ArrayList<>();

		for (WorldObject worldObject : worldObjects) {

			if (worldObject.getGroundState() == GroundState.ON
				&& (worldObject instanceof WorldObjectWithOutline)) {

				PolygonShapeXZ outline = null;

				try {
					outline = ((WorldObjectWithOutline)worldObject).
							getOutlinePolygonXZ();
				} catch (InvalidGeometryException e) {
					//ignore this outline
				}

				if (outline != null) {
					filterPolygons.add(outline);
				}

			}

		}

		/* perform filtering of positions */

		Iterator<VectorXZ> positionIterator = positions.iterator();

		while (positionIterator.hasNext()) {

			VectorXZ pos = positionIterator.next();

			for (PolygonShapeXZ filterPolygon : filterPolygons) {
				if (filterPolygon.contains(pos)) {
					positionIterator.remove();
					break;
				}
			}

		}

	}

}
