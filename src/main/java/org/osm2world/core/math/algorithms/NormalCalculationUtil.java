package org.osm2world.core.math.algorithms;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osm2world.core.math.TriangleXYZ;
import org.osm2world.core.math.TriangleXYZWithNormals;
import org.osm2world.core.math.VectorXYZ;

public final class NormalCalculationUtil {

	/** prevents instantiation */
	private NormalCalculationUtil() {}

	/**
	 * calculates normals for a collection of triangles
	 */
	public static final List<VectorXYZ> calculateTriangleNormals(
			List<VectorXYZ> vertices, boolean smooth) {

		assert vertices.size() % 3 == 0;

		VectorXYZ[] normals = new VectorXYZ[vertices.size()];

		//TODO: implement smooth case
		if (/*!smooth*/ true) { //flat

			for (int triangle = 0; triangle < vertices.size() / 3; triangle++) {

				int i = triangle * 3 + 1;

				VectorXYZ vBefore = vertices.get(i-1);
				VectorXYZ vAt = vertices.get(i);
				VectorXYZ vAfter = vertices.get(i+1);

				VectorXYZ toBefore = vBefore.subtract(vAt);
				VectorXYZ toAfter = vAfter.subtract(vAt);

				normals[i] = toBefore.crossNormalized(toAfter);

				normals[i-1] = normals[i];
				normals[i+1] = normals[i];

			}

		}

		return asList(normals);

	}


	public static final List<VectorXYZ> calculateTriangleStripNormals(
			List<VectorXYZ> vertices, boolean smooth) {

		assert vertices.size() >= 3;

		VectorXYZ[] normals = calculatePerTriangleNormals(vertices, false);
		return asList(normals);

		//TODO: implement smooth case

	}

	public static final List<VectorXYZ> calculateTriangleFanNormals(
			List<VectorXYZ> vertices, boolean smooth) {

		assert vertices.size() >= 3;

		VectorXYZ[] normals = calculatePerTriangleNormals(vertices, true);
		return asList(normals);

		//TODO: implement smooth case

	}

	/**
	 * calculates "flat" lighting normals for triangle strips and triangle fans
	 *
	 * @param vertices  fan/strip vertices
	 * @param fan       true for fans, false for strips
	 */
	private static VectorXYZ[] calculatePerTriangleNormals(
			List<VectorXYZ> vertices, boolean fan) {

		VectorXYZ[] normals = new VectorXYZ[vertices.size()];

		for (int triangle = 0; triangle < vertices.size() - 2; triangle++) {

			int i = triangle + 1;

			VectorXYZ vBefore = vertices.get( fan ? 0 : (i-1) );
			VectorXYZ vAt = vertices.get(i);
			VectorXYZ vAfter = vertices.get(i+1);

			VectorXYZ toBefore = vBefore.subtract(vAt);
			VectorXYZ toAfter = vAfter.subtract(vAt);

			if (triangle % 2 == 0 || fan) {
				normals[i+1] = toBefore.crossNormalized(toAfter);
			} else {
				normals[i+1] = toAfter.crossNormalized(toBefore);
			}

		}

		normals[0] = normals[2];
		normals[1] = normals[2];

		return normals;

	}

	private static final double MAX_ANGLE_RADIANS = Math.toRadians(75);

	/**
	 * calculates normals for vertices that are shared by multiple triangles.
	 */
	public static final Collection<TriangleXYZWithNormals> calculateTrianglesWithNormals(
			Collection<TriangleXYZ> triangles) {

		Map<VectorXYZ, List<TriangleXYZ>> adjacentTriangles =
			calculateAdjacentTriangles(triangles);

		Collection<TriangleXYZWithNormals> result =
			new ArrayList<TriangleXYZWithNormals>(triangles.size());

		for (TriangleXYZ triangle : triangles) {

			result.add(new TriangleXYZWithNormals(triangle,
					calculateNormal(triangle.v1, triangle, adjacentTriangles),
					calculateNormal(triangle.v2, triangle, adjacentTriangles),
					calculateNormal(triangle.v3, triangle, adjacentTriangles)));

		}

		return result;

	}

	private static VectorXYZ calculateNormal(VectorXYZ v, TriangleXYZ triangle,
			Map<VectorXYZ, List<TriangleXYZ>> adjacentTrianglesMap) {

		/* find adjacent triangles whose normals are close enough to that of t
		 * and save their normal vectors */

		List<VectorXYZ> relevantNormals = new ArrayList<VectorXYZ>();

		for (TriangleXYZ t2 : adjacentTrianglesMap.get(v)) {

			if (triangle == t2 ||
					triangle.getNormal().angleTo(t2.getNormal()) <= MAX_ANGLE_RADIANS) {

				//add, unless one of the existing normals is very similar

				boolean notCoplanar = true;
				for (VectorXYZ n : relevantNormals) {
					if (n.angleTo(t2.getNormal()) < 0.01) {
						notCoplanar = false;
						break;
					}
				}

				if (notCoplanar) {
					relevantNormals.add(t2.getNormal());
				}

			}
		}

		/* calculate sum of relevant normals,
		 * normalize it and set the result as normal for the vertex */

		VectorXYZ normal = new VectorXYZ(0, 0, 0);
		for (VectorXYZ addNormal : relevantNormals) {
			normal = normal.add(addNormal);
		}

		return normal.normalize();

	}

	private static Map<VectorXYZ, List<TriangleXYZ>> calculateAdjacentTriangles(
			Collection<TriangleXYZ> triangles) {

		Map<VectorXYZ, List<TriangleXYZ>> result =
			new HashMap<VectorXYZ, List<TriangleXYZ>>();

		for (TriangleXYZ triangle : triangles) {
			for (VectorXYZ vertex : triangle.getVertices()) {
				List<TriangleXYZ> triangleList = result.get(vertex);
				if (triangleList == null) {
					triangleList = new ArrayList<TriangleXYZ>();
					result.put(vertex, triangleList);
				}
				triangleList.add(triangle);
			}
		}

		return result;
	}

}
