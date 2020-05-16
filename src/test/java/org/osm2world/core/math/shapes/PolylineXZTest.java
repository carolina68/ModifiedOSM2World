package org.osm2world.core.math.shapes;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.osm2world.core.test.TestUtil.assertAlmostEquals;

import org.junit.Test;
import org.osm2world.core.math.VectorXZ;

public class PolylineXZTest {

	PolylineXZ p1 = new PolylineXZ(asList(
			new VectorXZ(-1, 0),
			new VectorXZ( 0, 0),
			new VectorXZ( 1, 0),
			new VectorXZ( 1, 3),
			new VectorXZ(-1, 3)
			));

	@Test
	public void testGetLength() {

		assertEquals(7, p1.getLength(), 1e-5);

	}

	@Test
	public void testOffsetOf() {

		/* vertices of the polyline */

		assertEquals(0, p1.offsetOf(new VectorXZ(-1, 0)), 1e-5);
		assertEquals(5, p1.offsetOf(new VectorXZ( 1, 3)), 1e-5);
		assertEquals(7, p1.offsetOf(new VectorXZ(-1, 3)), 1e-5);

		/* points on the vertices of the polyline */

		assertEquals(0.5, p1.offsetOf(new VectorXZ(-0.5, 0)), 1e-5);
		assertEquals(1.5, p1.offsetOf(new VectorXZ(+0.5, 0)), 1e-5);
		assertEquals(3.0, p1.offsetOf(new VectorXZ(1, 1)), 1e-5);

	}

	@Test
	public void testPointAtOffset() {

		assertAlmostEquals(-1, 0, p1.pointAtOffset(0));
		assertAlmostEquals( 1, 3, p1.pointAtOffset(5));
		assertAlmostEquals(-1, 3, p1.pointAtOffset(7));

		assertAlmostEquals(-0.5, 0, p1.pointAtOffset(0.5));
		assertAlmostEquals(+0.5, 0, p1.pointAtOffset(1.5));
		assertAlmostEquals(1, 1, p1.pointAtOffset(3.0));

	}

	/**
	 * tests that {@link PolylineXZ#offsetOf(VectorXZ)}
	 * is the inverse of {@link PolylineXZ#pointAtOffset(double)}
	 */
	@Test
	public void testOffsetSymmetry() {

		for (int i = 0; i <= p1.getLength() * 10; i++) {

			double offset = i * 0.1;

			assertEquals(offset, p1.offsetOf(p1.pointAtOffset(offset)), 1e-5);

		}

	}

}
