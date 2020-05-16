package org.osm2world.core.target.common.rendering;

import static java.lang.Math.PI;
import static org.osm2world.core.math.AxisAlignedBoundingBoxXZ.union;

import java.util.Arrays;
import java.util.List;

import org.osm2world.core.map_data.creation.MapProjection;
import org.osm2world.core.math.AxisAlignedBoundingBoxXZ;
import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.math.VectorXZ;

/**
 * calculates camera and projection information for orthographic tiles.
 */
public final class OrthoTilesUtil {

	/** 4 cardinal directions, can be used for camera placement */
	public static enum CardinalDirection {

		N, E, S, W;

		/**
		 * returns the closest cardinal direction for an angle
		 * @param angle  angle to north direction in radians;
		 *               consistent with {@link VectorXZ#angle()}
		 */
		public static CardinalDirection closestCardinal(double angle) {
			angle = angle % (2 * PI);
			if (angle < PI / 4) { return N; }
			else if (angle < 3 * PI / 4) { return E; }
			else if (angle < 5 * PI / 4) { return S; }
			else if (angle < 7 * PI / 4) { return W; }
			else { return N; }
		}

		public boolean isOppositeOf(CardinalDirection other) {
			return this == N && other == S
					|| this == E && other == W
					|| this == S && other == N
					|| this == W && other == E;
		}

	}

	/** prevents instantiation */
	private OrthoTilesUtil() { }

	public static final Camera cameraForTile(MapProjection mapProjection,
			TileNumber tile, double angleDeg, CardinalDirection from) {
		return cameraForBounds(boundsForTile(mapProjection, tile),
				angleDeg, from);
	}

	public static final Camera cameraForTiles(MapProjection mapProjection,
			List<TileNumber> tiles, double angleDeg, CardinalDirection from) {

		if (tiles.isEmpty()) { throw new IllegalArgumentException("empty tiles list"); }

		AxisAlignedBoundingBoxXZ result = boundsForTiles(mapProjection, tiles);

		return cameraForBounds(result, angleDeg, from);

	}

	public static final Camera cameraForBounds(
			AxisAlignedBoundingBoxXZ bounds, double angleDeg,
			CardinalDirection from) {

		Camera result = new Camera();

		VectorXYZ lookAt = new VectorXYZ(
				bounds.minX + bounds.sizeX() / 2,
				0,
				bounds.minZ + bounds.sizeZ() / 2);

		// calculate camera position (start with position for view from south,
		// then modify it depending on parameters)

		double cameraDistance = Math.max(bounds.sizeX(), bounds.sizeZ());

		double cameraOffsetX = 0;
		double cameraOffsetZ = - cameraDistance * Math.cos(Math.toRadians(angleDeg));

		if (from == CardinalDirection.W || from == CardinalDirection.E) {
			double temp = cameraOffsetX;
			cameraOffsetX = cameraOffsetZ;
			cameraOffsetZ = temp;
		}

		if (from == CardinalDirection.N || from == CardinalDirection.E) {
			cameraOffsetX = -cameraOffsetX;
			cameraOffsetZ = -cameraOffsetZ;
		}

		result.setCamera(lookAt.x + cameraOffsetX,
						 cameraDistance * Math.sin(Math.toRadians(angleDeg)),
						 lookAt.z + cameraOffsetZ,
						 lookAt.x, lookAt.y, lookAt.z);

		return result;
	}

	public static final Projection projectionForTile(MapProjection mapProjection,
			TileNumber tile, double angleDeg, CardinalDirection from) {
		AxisAlignedBoundingBoxXZ tileBounds = boundsForTile(mapProjection, tile);
		return projectionForBounds(tileBounds, angleDeg, from);
	}

	public static final Projection projectionForTiles(MapProjection mapProjection,
			List<TileNumber> tiles, double angleDeg, CardinalDirection from) {

		if (tiles.isEmpty()) { throw new IllegalArgumentException("empty tiles list"); }

		AxisAlignedBoundingBoxXZ result = boundsForTiles(mapProjection, tiles);

		return projectionForBounds(result, angleDeg, from);

	}

	public static final Projection projectionForBounds(
			AxisAlignedBoundingBoxXZ bounds, double angleDeg,
			CardinalDirection from) {

		double sin = Math.sin(Math.toRadians(angleDeg));

		double sizeX = bounds.sizeX();
		double sizeZ = bounds.sizeZ();

		if (from == CardinalDirection.W || from == CardinalDirection.E) {
			double temp = sizeX;
			sizeX = sizeZ;
			sizeZ = temp;
		}

		return new Projection(true,
				 sizeX / (sizeZ * sin),
				 Double.NaN,
				 sizeZ * sin,
				 -10000, 10000);

	}

	private static final AxisAlignedBoundingBoxXZ boundsForTile(
			MapProjection mapProjection, TileNumber tile) {

		VectorXZ tilePos1 = mapProjection.calcPos(
				tile2lat(tile.y, tile.zoom), tile2lon(tile.x, tile.zoom));

		VectorXZ tilePos2 = mapProjection.calcPos(
				tile2lat(tile.y+1, tile.zoom), tile2lon(tile.x+1, tile.zoom));

		return new AxisAlignedBoundingBoxXZ(Arrays.asList(tilePos1, tilePos2));

	}

	public static final AxisAlignedBoundingBoxXZ boundsForTiles(
			MapProjection mapProjection, List<TileNumber> tiles) {

		AxisAlignedBoundingBoxXZ result = boundsForTile(mapProjection, tiles.get(0));

		for (int i=1; i<tiles.size(); i++) {
			AxisAlignedBoundingBoxXZ newBox = boundsForTile(mapProjection, tiles.get(i));
			result = union(result, newBox);
		}

		return result;

	}

	private static final double tile2lon(int x, int z) {
		return x / Math.pow(2.0, z) * 360.0 - 180;
	}

	private static final double tile2lat(int y, int z) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

}
