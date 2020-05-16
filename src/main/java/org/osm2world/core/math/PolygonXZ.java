package org.osm2world.core.math;

import static org.osm2world.core.math.GeometryUtil.distanceFromLineSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.osm2world.core.math.shapes.ShapeXZ;

public class PolygonXZ implements ShapeXZ {

	/** polygon vertices; first and last vertex are equal */
	protected final List<VectorXZ> vertexLoop;

	/**
	 * @param vertexLoop  vertices defining the polygon;
	 *                    first and last vertex must be equal
	 * @throws InvalidGeometryException  if the polygon is self-intersecting
	 *                                   or produces invalid area calculation results
	 */
	public PolygonXZ(List<VectorXZ> vertexLoop) {

		assertLoopProperty(vertexLoop);

		this.vertexLoop = vertexLoop;

	}

	/**
	 * returns the number of vertices in this polygon.
	 * The duplicated first/last vertex is <em>not</em> counted twice,
	 * so the result is equivalent to {@link #getVertices()}.size().
	 */
	public int size() {
		return vertexLoop.size()-1;
	}

	/**
	 * returns the polygon's vertices.
	 * Unlike {@link #getVertexList()}, there is no duplication
	 * of the first/last vertex.
	 */
	public List<VectorXZ> getVertices() {
		return vertexLoop.subList(0, vertexLoop.size()-1);
	}

	/**
	 * returns the polygon's vertices. First and last vertex are equal.
	 *
	 * @return list of vertices, not empty, not null
	 */
	@Override
	public List<VectorXZ> getVertexList() {
		return vertexLoop;
	}

	/**
	 * @deprecated Use the equivalent {@link #getVertexList()} instead.
	 */
	public List<VectorXZ> getVertexLoop() {
		return vertexLoop;
	}

	/**
	 * returns a collection that contains all vertices of this polygon
	 * at least once. Can be used if you don't care about whether the first/last
	 * vector is duplicated.
	 */
	public List<VectorXZ> getVertexCollection() {
		return vertexLoop;
	}

	/**
	 * returns the vertex at a position in the vertex sequence
	 */
	public VectorXZ getVertex(int index) {
		assert 0 <= index && index < vertexLoop.size()-1;
		return vertexLoop.get(index);
	}

	/**
	 * returns the successor of the vertex at a position in the vertex sequence.
	 * This wraps around the vertex loop, so the successor of the last vertex
	 * is the first vertex.
	 */
	public VectorXZ getVertexAfter(int index) {
		assert 0 <= index && index < vertexLoop.size()-1;
		return getVertex((index + 1) % size());
	}

	/**
	 * returns the predecessor of the vertex at a position in the vertex sequence.
	 * This wraps around the vertex loop, so the predecessor of the first vertex
	 * is the last vertex.
	 */
	public VectorXZ getVertexBefore(int index) {
		assert 0 <= index && index < vertexLoop.size()-1;
		return getVertex((index + size() - 1) % size());
	}

	/**
	 * returns a subsection of the polygon's outline.
	 * This treats the outline as a loop, so endIndex is allowed to be less than startIndex.
	 */
	public List<VectorXZ> getVertices(int startIndex, int endIndex) {

		assert 0 <= startIndex && startIndex < size();
		assert 0 <= endIndex && endIndex < size();

		List<VectorXZ> result = new ArrayList<>();

		if (startIndex != endIndex) {
			for (int i = startIndex; i != endIndex; i = (i+1) % size()) {
				result.add(getVertex(i));
			}
		}

		result.add(getVertex(endIndex));

		return result;

	}

	/**
	 * returns the polygon segment with minimum distance to a given point
	 */
	public LineSegmentXZ getClosestSegment(VectorXZ point) {

		LineSegmentXZ closestSegment = null;
		double closestDistance = Double.MAX_VALUE;

		for (LineSegmentXZ segment : getSegments()) {
			double distance = distanceFromLineSegment(point, segment);
			if (distance < closestDistance) {
				closestSegment = segment;
				closestDistance = distance;
			}
		}

		return closestSegment;

	}

	/**
	 * returns whether this polygon is self-intersecting
	 */
	public boolean isSelfIntersecting() {
		return isSelfIntersecting(vertexLoop);
	}

	/**
	 * returns true if the polygon defined by the polygonVertexLoop parameter
	 * is self-intersecting.<br/>
	 * The Code is based on Shamos-Hoey's algorithm
	 *
	 * TODO: if the end vertex of two line segments are the same the
	 *       polygon is never considered as self intersecting on purpose.
	 *       This behavior should probably be reconsidered, but currently
	 *       left as is due to frequent cases of such polygons.
	 */
	public static boolean isSelfIntersecting(List<VectorXZ> polygonVertexLoop) {

		final class Event {
			boolean start;
			LineSegmentXZ line;

			Event(LineSegmentXZ l, boolean s) {
				this.line = l;
				this.start = s;
			}
		}

		// we have n-1 vertices as the first and last vertex are the same
		final int segments = polygonVertexLoop.size()-1;

		// generate an array of input events associated with their line segments
		Event[] events = new Event[segments*2];
		for (int i = 0; i < segments; i++) {
			VectorXZ v1 = polygonVertexLoop.get(i);
			VectorXZ v2 = polygonVertexLoop.get(i+1);

			// Create a line where the first vertex is left (or above) the second vertex
			LineSegmentXZ line;
			if ((v1.x < v2.x) || ((v1.x == v2.x) && (v1.z < v2.z))) {
				line = new LineSegmentXZ(v1, v2);
			} else {
				line = new LineSegmentXZ(v2, v1);
			}

			events[2*i] = new Event(line, true);
			events[2*i+1] = new Event(line, false);
		}

		// sort the input events according to the x-coordinate, then z-coordinate
		Arrays.sort(events, new Comparator<Event>() {
			public int compare(Event e1, Event e2) {

				VectorXZ v1 = e1.start? e1.line.p1 : e1.line.p2;
				VectorXZ v2 = e2.start? e2.line.p1 : e2.line.p2;

				if (v1.x < v2.x) return -1;
				else if (v1.x == v2.x) {
					if (v1.z < v2.z) return -1;
					else if (v1.z == v2.z) return 0;
				}
				return 1;
			}});

		// A TreeSet, used for the sweepline algorithm
		TreeSet<LineSegmentXZ> sweepLine = new TreeSet<LineSegmentXZ>(new Comparator<LineSegmentXZ>() {
			public int compare(LineSegmentXZ l1, LineSegmentXZ l2) {

				VectorXZ v1 = l1.p1;
				VectorXZ v2 = l2.p1;

				if (v1.z < v2.z) return -1;
				else if (v1.z == v2.z) {
					if (v1.x < v2.x) return -1;
					else if (v1.x == v2.x) {
						if (l1.p2.z < l2.p2.z) return -1;
						else if (l1.p2.z == l2.p2.z) {
							if (l1.p2.x < l2.p2.x) return -1;
							else if (l1.p2.x == l2.p2.x) return 0;
						}
					}
				}
				return 1;
			}});

		// start the algorithm by visiting every event
		for (Event event : events) {
			LineSegmentXZ line = event.line;

			if (event.start) { // if it is a startpoint

				LineSegmentXZ lower = sweepLine.lower(line);
				LineSegmentXZ higher = sweepLine.higher(line);

				sweepLine.add(line);

				if (lower != null) {
					if (lower.intersects(line.p1, line.p2)) {
						return true;
					}
				}

				if (higher != null) {
					if (higher.intersects(line.p1, line.p2)) {
						return true;
					}
				}
			} else { // if it is an endpoint

				LineSegmentXZ lower = sweepLine.lower(line);
				LineSegmentXZ higher = sweepLine.higher(line);

				sweepLine.remove(line);

				if ((lower == null) || (higher == null)) {
					continue;
				}

				if (lower.intersects(higher.p1, higher.p2)) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * checks whether this polygon is simple
	 */
	public boolean isSimple() {
		try {
			this.asSimplePolygon();
			return true;
		} catch (InvalidGeometryException e) {
			return false;
		}
	}

	/**
	 * returns a polygon with the coordinates of this polygon
	 * that is an instance of {@link SimplePolygonXZ}.
	 * Only works if it actually {@link #isSimple()}!
	 */
	public SimplePolygonXZ asSimplePolygon() {
		return new SimplePolygonXZ(vertexLoop);
	}

	/**
	 * returns a triangle with the same vertices as this polygon.
	 * Requires that the polygon is triangular!
	 */
	public TriangleXZ asTriangleXZ() {
		if (vertexLoop.size() != 4) {
			throw new InvalidGeometryException("attempted creation of triangle " +
					"from polygon with vertex loop of size " + vertexLoop.size() +
					": " + vertexLoop);
		} else {
			return new TriangleXZ(
					vertexLoop.get(0),
					vertexLoop.get(1),
					vertexLoop.get(2));
		}
	}

	public PolygonXYZ xyz(final double y) {
		return new PolygonXYZ(VectorXZ.listXYZ(vertexLoop, y));
	}

	public PolygonXZ reverse() {
		List<VectorXZ> newVertexLoop = new ArrayList<VectorXZ>(vertexLoop);
		Collections.reverse(newVertexLoop);
		return new PolygonXZ(newVertexLoop);
	}

	/**
	 * returns the average of all vertex coordinates.
	 * The result is not necessarily contained by this polygon.
	 */
	public VectorXZ getCenter() {
		double x=0, z=0;
		int numberVertices = vertexLoop.size()-1;
		for (VectorXZ vertex : getVertices()) {
			x += vertex.x / numberVertices;
			z += vertex.z / numberVertices;
			/* single division per coordinate after loop would be faster,
			 * but might cause numbers to get too large */
		}
		return new VectorXZ(x, z);
	}

	/**
	 * returns the length of the polygon's outline.
	 * (This does <em>not</em> return the number of vertices,
	 * but the sum of distances between subsequent nodes.)
	 */
	public double getOutlineLength() {
		double length = 0;
		for (int i = 0; i+1 < vertexLoop.size(); i++) {
			length += VectorXZ.distance(vertexLoop.get(i), vertexLoop.get(i+1));
		}
		return length;
	}

	/**
	 * returns true if the other polygon has the same vertices in the same order,
	 * possibly with a different start vertex
	 */
	public boolean isEquivalentTo(PolygonXZ other) {

		if (vertexLoop.size() != other.vertexLoop.size()) {
			return false;
		}

		List<VectorXZ> ownVertices = getVertices();
		List<VectorXZ> otherVertices = other.getVertices();

		for (int offset = 0; offset < ownVertices.size(); offset ++) {

			boolean matches = true;

			for (int i = 0; i < ownVertices.size(); i++) {
				int iWithOffset = (i + offset) % ownVertices.size();
				if (!otherVertices.get(i).equals(ownVertices.get(iWithOffset))) {
					matches = false;
					break;
				}
			}

			if (matches) {
				return true;
			}

		}

		return false;

	}

	/**
	 * checks that the first and last vertex of the vertex list are equal.
	 * @throws IllegalArgumentException  if first and last vertex aren't equal
	 *                                   (this is usually a programming error,
	 *                                    therefore InvalidGeometryException is not used)
	 */
	protected static void assertLoopProperty(List<VectorXZ> vertexLoop) {
		if (!vertexLoop.get(0).equals(vertexLoop.get(vertexLoop.size() - 1))) {
			throw new IllegalArgumentException("first and last vertex must be equal\n"
					+ "Polygon vertices: " + vertexLoop);
		}
	}

	@Override
	public String toString() {
		return vertexLoop.toString();
	}

}
