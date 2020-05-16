package org.osm2world.core.math.algorithms;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.osm2world.core.test.TestUtil.assertSameCyclicOrder;

import java.util.List;

import org.junit.Test;
import org.osm2world.core.math.SimplePolygonXZ;
import org.osm2world.core.math.TriangleXZ;
import org.osm2world.core.math.VectorXZ;

public class Earcut4JTriangulationTest {

	@Test
	public void testTriangulate_triangle() {

		SimplePolygonXZ outer = new SimplePolygonXZ(asList(
				new VectorXZ(-1, 0),
				new VectorXZ(1, 0),
				new VectorXZ(0, 1),
				new VectorXZ(-1, 0)
				));

		List<TriangleXZ> result = Earcut4JTriangulationUtil.triangulate(outer, emptyList());

		assertEquals(1, result.size());
		assertSameCyclicOrder(true, result.get(0).getVertices(), outer.getVertex(0), outer.getVertex(1), outer.getVertex(2));

	}

	@Test
	public void testTriangulate_triangleWithPoint() {

		SimplePolygonXZ outer = new SimplePolygonXZ(asList(
				new VectorXZ(-1, 0),
				new VectorXZ(1, 0),
				new VectorXZ(0, 1),
				new VectorXZ(-1, 0)
				));

		VectorXZ point = new VectorXZ(0, 0.3);

		List<TriangleXZ> result = Earcut4JTriangulationUtil.triangulate(outer, emptyList(), asList(point));

		assertEquals(3, result.size());

	}

	@Test
	public void testTriangulate_rectangle() {

		SimplePolygonXZ outer = new SimplePolygonXZ(asList(
				new VectorXZ(0, 0),
				new VectorXZ(1, 0),
				new VectorXZ(1, 1),
				new VectorXZ(0, 1),
				new VectorXZ(0, 0)
				));

		List<TriangleXZ> result = Earcut4JTriangulationUtil.triangulate(outer, emptyList());

		assertEquals(2, result.size());

	}

	@Test
	public void testTriangulate_rectangleWithHole() {

		SimplePolygonXZ outer = new SimplePolygonXZ(asList(
				new VectorXZ(0, 0),
				new VectorXZ(1, 0),
				new VectorXZ(1, 1),
				new VectorXZ(0, 1),
				new VectorXZ(0, 0)
				));

		SimplePolygonXZ inner = new SimplePolygonXZ(asList(
				new VectorXZ(0.25, 0.25),
				new VectorXZ(0.75, 0.25),
				new VectorXZ(0.75, 0.75),
				new VectorXZ(0.25, 0.75),
				new VectorXZ(0.25, 0.25)
				));

		List<TriangleXZ> result = Earcut4JTriangulationUtil.triangulate(outer, asList(inner));

		assertEquals(8, result.size());

	}

}
