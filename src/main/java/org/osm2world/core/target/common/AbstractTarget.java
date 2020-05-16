package org.osm2world.core.target.common;

import static com.google.common.collect.Lists.reverse;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.osm2world.core.math.GeometryUtil.*;
import static org.osm2world.core.math.VectorXYZ.*;
import static org.osm2world.core.target.common.ExtrudeOption.*;
import static org.osm2world.core.target.common.material.NamedTexCoordFunction.*;
import static org.osm2world.core.target.common.material.TexCoordUtil.texCoordLists;
import static org.osm2world.core.world.modules.common.WorldModuleGeometryUtil.transformShape;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.osm2world.core.math.SimplePolygonXZ;
import org.osm2world.core.math.TriangleXZ;
import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.math.shapes.CircleXZ;
import org.osm2world.core.math.shapes.ShapeXZ;
import org.osm2world.core.math.shapes.SimpleClosedShapeXZ;
import org.osm2world.core.target.Renderable;
import org.osm2world.core.target.Target;
import org.osm2world.core.target.common.material.Material;
import org.osm2world.core.world.data.WorldObject;

/**
 * superclass for {@link Target} implementations that defines some
 * of the required methods using others. Extending it reduces the number of
 * methods that have to be provided by the implementation
 */
public abstract class AbstractTarget<R extends Renderable>
		implements Target<R> {

	protected Configuration config;

	@Override
	public void setConfiguration(Configuration config) {
		this.config = config;
	}

	@Override
	public void beginObject(WorldObject object) {}

	@Override
	public void drawShape(Material material, SimpleClosedShapeXZ shape, VectorXYZ point,
			VectorXYZ frontVector, VectorXYZ upVector, double scaleFactor) {

		for (TriangleXZ triangle : shape.getTriangulation()) {

			List<VectorXYZ> triangleVertices = new ArrayList<VectorXYZ>();

			for (VectorXZ v : triangle.getVertexList()) {
				triangleVertices.add(new VectorXYZ(-v.x, v.z, 0));
			}

			if (scaleFactor != 1.0) {
				triangleVertices = scaleShapeVectors(triangleVertices, scaleFactor);
			}

			triangleVertices = transformShape(
					triangleVertices, point, frontVector, upVector);

			//TODO better default texture coordinate function
			drawTriangleStrip(material, triangleVertices.subList(0, 3),
					texCoordLists(triangleVertices.subList(0, 3), material, GLOBAL_X_Y));

		}

	}

	/**
	 * draws an extruded shape using {@link #drawTriangleStrip(Material, List, List)} calls.
	 * See {@link Target#drawExtrudedShape(Material, ShapeXZ, List, List, List, List, EnumSet)}
	 * for documentation of the implemented interface method.
	 */
	@Override
	public void drawExtrudedShape(Material material, ShapeXZ shape, List<VectorXYZ> path,
			List<VectorXYZ> upVectors, List<Double> scaleFactors,
			List<List<VectorXZ>> texCoordLists, EnumSet<ExtrudeOption> options) {

		/* validate arguments */

		if (path.size() < 2) {
			throw new IllegalArgumentException("path needs at least 2 nodes");
		} else if (upVectors != null && path.size() != upVectors.size()) {
			throw new IllegalArgumentException("path and upVectors must have same size");
		} else if (scaleFactors != null && path.size() != scaleFactors.size()) {
			throw new IllegalArgumentException("path and scaleFactors must have same size");
		}

		if (upVectors == null) {
			throw new NullPointerException("upVectors must not be null");
		}

		if (texCoordLists != null) {
			for (List<VectorXZ> texCoordList : texCoordLists) {
				if (texCoordList.size() != path.size() * shape.getVertexList().size()) {
					throw new IllegalArgumentException("incorrect number of texture coordinates");
				}
			}
		}

		/* provide defaults for optional parameters */

		if (scaleFactors == null) {
			scaleFactors = nCopies(path.size(), DEFAULT_SCALE_FACTOR);
		}

		if (options == null) {
			options = DEFAULT_EXTRUDE_OPTIONS;
		}

		/* obtain the shape's vertices */

		List<VectorXYZ> shapeVertices = new ArrayList<VectorXYZ>();

		for (VectorXZ v : shape.getVertexList()) {
			shapeVertices.add(new VectorXYZ(-v.x, v.z, 0));
		}

		/* calculate the forward direction of the shape from the path.
		 * Special handling for the first and last point,
		 * where the calculation of the "forward" vector is different. */

		List<VectorXYZ> forwardVectors = new ArrayList<VectorXYZ>(path.size());

		forwardVectors.add(path.get(1).subtract(path.get(0)).normalize());

		for (int pathI = 1; pathI < path.size() - 1; pathI ++) {

			VectorXYZ forwardVector = path.get(pathI+1).subtract(path.get(pathI-1));
			forwardVectors.add(forwardVector.normalize());

		}

		int last = path.size() - 1;
		forwardVectors.add(path.get(last).subtract(path.get(last-1)).normalize());

		/* create an instance of the shape at each point of the path. */

		@SuppressWarnings("unchecked")
		List<VectorXYZ>[] shapeVectors = new List[path.size()];

		for (int pathI = 0; pathI < path.size(); pathI ++) {

			shapeVectors[pathI] = transformShape(
					scaleShapeVectors(shapeVertices, scaleFactors.get(pathI)),
					path.get(pathI),
					forwardVectors.get(pathI),
					upVectors.get(pathI));

		}

		/* draw triangle strips */

		for (int i = 0; i+1 < shapeVertices.size(); i++) {

			VectorXYZ[] triangleStripVectors = new VectorXYZ[2*shapeVectors.length];

			List<List<VectorXZ>> stripTexCoords = null;

			if (texCoordLists != null) {

				stripTexCoords = new ArrayList<List<VectorXZ>>();

				for (int texLayer = 0; texLayer < texCoordLists.size(); texLayer ++) {
					stripTexCoords.add(new ArrayList<VectorXZ>());
				}

			}

			for (int j = 0; j < shapeVectors.length; j++) {

				triangleStripVectors[j*2+0] = shapeVectors[j].get(i);
				triangleStripVectors[j*2+1] = shapeVectors[j].get(i+1);

				if (texCoordLists != null) {

					int index = j * shapeVectors[0].size() + i;

					for (int texLayer = 0; texLayer < texCoordLists.size(); texLayer ++) {
						stripTexCoords.get(texLayer).add(texCoordLists.get(texLayer).get(index));
						stripTexCoords.get(texLayer).add(texCoordLists.get(texLayer).get(index + 1));
					}
				}

			}

			List<VectorXYZ> strip = asList(triangleStripVectors);

			if (stripTexCoords == null) {
				stripTexCoords = texCoordLists(strip, material, STRIP_WALL);
			}

			drawTriangleStrip(material, strip, stripTexCoords);

		}

		/* draw caps (if requested in the options and possible for this shape) */

		if (shape instanceof SimpleClosedShapeXZ) {

			if (options.contains(START_CAP) && scaleFactors.get(0) > 0) {
				drawShape(material, new SimplePolygonXZ(reverse(shape.getVertexList())), // invert winding
						path.get(0), forwardVectors.get(0), upVectors.get(0), scaleFactors.get(0));
			}

			if (options.contains(END_CAP) && scaleFactors.get(last) > 0) {
				drawShape(material, (SimpleClosedShapeXZ)shape,
						path.get(last), forwardVectors.get(last), upVectors.get(last), scaleFactors.get(last));
			}

		}

	}

	private static final Double DEFAULT_SCALE_FACTOR = Double.valueOf(1.0);

	private static final EnumSet<ExtrudeOption> DEFAULT_EXTRUDE_OPTIONS = EnumSet.noneOf(ExtrudeOption.class);

	private static final List<VectorXYZ> scaleShapeVectors(List<VectorXYZ> vs, double scale) {

		if (scale == 1) {

			return vs;

		} else if (scale == 0) {

			return nCopies(vs.size(), NULL_VECTOR);

		} else {

			List<VectorXYZ> result = new ArrayList<VectorXYZ>(vs.size());

			for (VectorXYZ v : vs) {
				result.add(v.mult(scale));
			}

			return result;

		}

	}

	@Override
	public void drawBox(Material material,
			VectorXYZ bottomCenter, VectorXZ faceDirection,
			double height, double width, double depth) {

		final VectorXYZ backVector = faceDirection.mult(-depth).xyz(0);
		final VectorXYZ rightVector = faceDirection.rightNormal().mult(-width).xyz(0);
		final VectorXYZ upVector = new VectorXYZ(0, height, 0);

		final VectorXYZ frontLowerLeft = bottomCenter
				.add(rightVector.mult(-0.5))
				.add(backVector.mult(-0.5));

		final VectorXYZ frontLowerRight = frontLowerLeft.add(rightVector);
		final VectorXYZ frontUpperLeft  = frontLowerLeft.add(upVector);
		final VectorXYZ frontUpperRight = frontLowerRight.add(upVector);

		final VectorXYZ backLowerLeft   = frontLowerLeft.add(backVector);
		final VectorXYZ backLowerRight  = frontLowerRight.add(backVector);
		final VectorXYZ backUpperLeft   = frontUpperLeft.add(backVector);
		final VectorXYZ backUpperRight  = frontUpperRight.add(backVector);

		List<VectorXYZ> vsStrip1 = asList(
				backLowerLeft, backLowerRight,
				frontLowerLeft, frontLowerRight,
				frontUpperLeft, frontUpperRight,
				backUpperLeft, backUpperRight
		);

		List<VectorXYZ> vsStrip2 = asList(
				frontUpperRight, frontLowerRight,
				backUpperRight, backLowerRight,
				backUpperLeft, backLowerLeft,
				frontUpperLeft, frontLowerLeft
		);

		List<List<VectorXZ>> texCoords1 = null, texCoords2 = null;

		if (material.getTextureDataList() != null) {
			texCoords1 = nCopies(material.getTextureDataList().size(), BOX_TEX_COORDS_1);
			texCoords2 = nCopies(material.getTextureDataList().size(), BOX_TEX_COORDS_2);
		}

		drawTriangleStrip(material, vsStrip1, texCoords1);
		drawTriangleStrip(material, vsStrip2, texCoords2);

	}

	protected static final List<VectorXZ> BOX_TEX_COORDS_1 = asList(
		new VectorXZ(0,     0), new VectorXZ(0.25,     0),
		new VectorXZ(0, 1.0/3), new VectorXZ(0.25, 1.0/3),
		new VectorXZ(0, 2.0/3), new VectorXZ(0.25, 2.0/3),
		new VectorXZ(0,     1), new VectorXZ(0.25,     1)
	);

	protected static final List<VectorXZ> BOX_TEX_COORDS_2 = asList(
		new VectorXZ(0.25, 2.0/3), new VectorXZ(0.25, 1.0/3),
		new VectorXZ(0.50, 2.0/3), new VectorXZ(0.50, 1.0/3),
		new VectorXZ(0.75, 2.0/3), new VectorXZ(0.75, 1.0/3),
		new VectorXZ(1.00, 2.0/3), new VectorXZ(1.00, 1.0/3)
	);

	/**
	 * See {@link Target#drawColumn(Material, Integer, VectorXYZ, double, double, double, boolean, boolean)}.
	 * Implemented using {@link #drawExtrudedShape(Material, ShapeXZ, List, List, List, List, EnumSet)}.
	 */
	@Override
	public void drawColumn(Material material, Integer corners, VectorXYZ base,
			double height, double radiusBottom, double radiusTop,
			boolean drawBottom, boolean drawTop) {

		CircleXZ bottomCircle = new CircleXZ(VectorXZ.NULL_VECTOR, radiusBottom);
		ShapeXZ bottomShape;

		if (corners == null) {

			material = material.makeSmooth();
			bottomShape = bottomCircle;

		} else {

			bottomShape = new SimplePolygonXZ(bottomCircle.getVertexList(corners));

		}

		EnumSet<ExtrudeOption> options = EnumSet.noneOf(ExtrudeOption.class);

		if (drawBottom) { options.add(START_CAP); }
		if (drawTop) { options.add(END_CAP); }

		drawExtrudedShape(material, bottomShape, asList(base, base.addY(height)),
				nCopies(2, Z_UNIT), asList(1.0, radiusTop/radiusBottom), null, options);

	}

	@Override
	public void drawTriangleStrip(Material material, List<VectorXYZ> vs,
			List<List<VectorXZ>> texCoordLists) {

		List<List<VectorXZ>> newTexCoordLists = emptyList();
		if (texCoordLists != null && !texCoordLists.isEmpty()) {
			newTexCoordLists = new ArrayList<List<VectorXZ>>(texCoordLists.size());
			for (List<VectorXZ> texCoordList : texCoordLists) {
				newTexCoordLists.add(
						triangleVertexListFromTriangleStrip(texCoordList));
			}
		}

		drawTriangles(material, trianglesFromTriangleStrip(vs), newTexCoordLists);
	}

	@Override
	public void drawTriangleFan(Material material, List<VectorXYZ> vs,
			List<List<VectorXZ>> texCoordLists) {

		List<List<VectorXZ>> newTexCoordLists = emptyList();
		if (texCoordLists != null && !texCoordLists.isEmpty()) {
			newTexCoordLists = new ArrayList<List<VectorXZ>>(texCoordLists.size());
			for (List<VectorXZ> texCoordList : texCoordLists) {
				newTexCoordLists.add(
						triangleVertexListFromTriangleFan(texCoordList));
			}
		}

		drawTriangles(material, trianglesFromTriangleFan(vs), newTexCoordLists);

	}

	@Override
	public void drawConvexPolygon(Material material, List<VectorXYZ> vs,
			List<List<VectorXZ>> texCoordLists) {
		drawTriangleFan(material, vs, texCoordLists);
	}

	@Override
	public void finish() {}

}
