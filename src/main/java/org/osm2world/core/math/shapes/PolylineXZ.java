package org.osm2world.core.math.shapes;

import static java.util.Arrays.asList;
import static org.osm2world.core.math.VectorXZ.distance;

import java.util.ArrayList;
import java.util.List;

import org.osm2world.core.math.LineSegmentXZ;
import org.osm2world.core.math.VectorXZ;

/**
 * a polyline (aka linestring)
 */
public class PolylineXZ implements PolylineShapeXZ {

	private final List<VectorXZ> vertices;

	public PolylineXZ(List<VectorXZ> vertices) {
		this.vertices = vertices;
	}

	public PolylineXZ(VectorXZ... vertices) {
		this(asList(vertices));
	}

	@Override
	public List<VectorXZ> getVertexList() {
		return vertices;
	}

	@Override
	public List<LineSegmentXZ> getSegments() {

		List<LineSegmentXZ> segments = new ArrayList<LineSegmentXZ>(vertices.size() - 1);

		for (int i=0; i+1 < vertices.size(); i++) {
			segments.add(new LineSegmentXZ(vertices.get(i), vertices.get(i+1)));
		}

		return segments;

	}

	@Override
	public double getLength() {

		double length = 0;

		for (int i = 0; i + 1 < vertices.size(); i++) {
			length += distance(vertices.get(i), vertices.get(i+1));
		}

		return length;

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PolylineXZ) {
			return vertices.equals(((PolylineXZ)obj).vertices);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return vertices.hashCode();
	}

	@Override
	public String toString() {
		return vertices.toString();
	}

}
