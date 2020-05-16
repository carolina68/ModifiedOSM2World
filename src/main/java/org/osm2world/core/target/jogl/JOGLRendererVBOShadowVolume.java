package org.osm2world.core.target.jogl;

import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL2GL3.GL_DOUBLE;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL3;

import org.osm2world.core.math.VectorXYZW;
import org.osm2world.core.target.common.Primitive;
import org.osm2world.core.target.common.material.Material;
import org.osm2world.core.target.common.material.Material.Shadow;
import org.osm2world.core.target.common.material.Material.Transparency;

import com.jogamp.common.nio.Buffers;

/**
 * Renders the shadow volumes for the contents of a {@link PrimitiveBuffer} using JOGL.
 * Uses vertex buffer objects (VBO) to speed up the process.
 *
 * If you don't need the renderer anymore, it's recommended to manually call
 * {@link #freeResources()} to delete the VBOs and other resources.
 */
public class JOGLRendererVBOShadowVolume  {

	protected GL3 gl;
	protected AbstractPrimitiveShader shader;
	protected VectorXYZW lightPos;

	protected static final boolean DOUBLE_PRECISION_RENDERING = false;

	/** VBOs with static, non-alphablended geometry for each material */
	protected List<VBODataShadowVolume<?>> vbos = new ArrayList<VBODataShadowVolume<?>>();

	private final class VBODataDouble extends VBODataShadowVolume<DoubleBuffer> {

		public VBODataDouble(GL3 gl, Collection<Primitive> primitives, VectorXYZW lightPos) {
			super(gl, primitives, lightPos);
		}

		@Override
		protected DoubleBuffer createBuffer(int numValues) {
			return Buffers.newDirectDoubleBuffer(numValues);
		}

		@Override
		protected void put(DoubleBuffer buffer, VectorXYZW t) {
			buffer.put(t.x);
			buffer.put(t.y);
			buffer.put(-t.z);
			buffer.put(t.w);
		}

		@Override
		protected int valueTypeSize() {
			return Buffers.SIZEOF_DOUBLE;
		}

		@Override
		protected int glValueType() {
			return GL_DOUBLE;
		}

	}

	private final class VBODataFloat extends VBODataShadowVolume<FloatBuffer> {

		public VBODataFloat(GL3 gl, Collection<Primitive> primitives, VectorXYZW lightPos) {
			super(gl, primitives, lightPos);
		}

		@Override
		protected FloatBuffer createBuffer(int numValues) {
			return Buffers.newDirectFloatBuffer(numValues);
		}

		@Override
		protected void put(FloatBuffer buffer, VectorXYZW t) {
			buffer.put((float)t.x);
			buffer.put((float)t.y);
			buffer.put((float)-t.z);
			buffer.put((float)t.w);
		}

		@Override
		protected int valueTypeSize() {
			return Buffers.SIZEOF_FLOAT;
		}

		@Override
		protected int glValueType() {
			return GL_FLOAT;
		}

	}

	/**
	 * Creates the shadow volumes (and VBOs) for all primitives in a {@link PrimitiveBuffer}.
	 * @param primitiveBuffer the primitives to create the shadow volumes for
	 * @param lightPos the position of the light source to create the shadow volumes for. If ligthPose.w == 0, the light is directional.
	 */
	JOGLRendererVBOShadowVolume(GL3 gl, PrimitiveBuffer primitiveBuffer, VectorXYZW lightPos) {

		this.gl = gl;
		this.lightPos = lightPos;
		this.init(primitiveBuffer);
	}

	protected void init(PrimitiveBuffer primitiveBuffer) {

		Collection<Primitive> combinedPrimitives = new ArrayList<Primitive>();
		for (Material material : primitiveBuffer.getMaterials()) {

			if (material.getTransparency() == Transparency.FALSE && material.getShadow() == Shadow.TRUE) {
				Collection<Primitive> primitives = primitiveBuffer.getPrimitives(material);
				combinedPrimitives.addAll(primitives);
			}

		}
		vbos.add(this.createVBOData(combinedPrimitives));

	}

	VBODataShadowVolume<?> createVBOData(Collection<Primitive> primitives) {
		if (DOUBLE_PRECISION_RENDERING)
			return new VBODataDouble(gl, primitives, lightPos);
		else
			return new VBODataFloat(gl, primitives, lightPos);
	}

	/**
	 * Render all shadow volume VBOs
	 */
	public void render() {

		/* render static geometry */

		shader.glEnableVertexAttribArray(shader.getVertexPositionID());

		for (VBODataShadowVolume<?> vboData : vbos) {
			vboData.setShader(shader);
			vboData.render();
		}

		shader.glDisableVertexAttribArray(shader.getVertexPositionID());

	}

	/**
	 * frees all OpenGL resources associated with this object. Rendering will no longer be possible afterwards!
	 */
	public void freeResources() {
		gl = null;
		if (vbos != null) {
			for (VBODataShadowVolume<?> vbo : vbos) {
				vbo.delete();
			}
			vbos = null;
		}
	}

	/**
	 * Set the shader used for rendering.
	 */
	public void setShader(AbstractPrimitiveShader shader) {
		this.shader = shader;
	}
}
