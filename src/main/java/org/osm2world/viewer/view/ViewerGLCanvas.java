package org.osm2world.viewer.view;

import java.awt.Color;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import org.osm2world.core.target.jogl.AbstractJOGLTarget;
import org.osm2world.viewer.model.Data;
import org.osm2world.viewer.model.MessageManager;
import org.osm2world.viewer.model.MessageManager.Message;
import org.osm2world.viewer.model.RenderOptions;
import org.osm2world.viewer.view.debug.DebugView;
import org.osm2world.viewer.view.debug.HelpView;
import org.osm2world.viewer.view.debug.WorldObjectView;

import com.jogamp.opengl.util.FPSAnimator;

public class ViewerGLCanvas extends GLCanvas {

	private static final long serialVersionUID = 817150566654010861L;

	public ViewerGLCanvas(Data data, MessageManager messageManager, RenderOptions renderOptions, GLCapabilities capabilities) {
		super(capabilities);

		setSize(800, 600);
		setIgnoreRepaint(true);

		addGLEventListener(new ViewerGLEventListener(data, messageManager, renderOptions));

		FPSAnimator animator = new FPSAnimator( this, 60 );

		animator.start();

	}


	public class ViewerGLEventListener implements GLEventListener {

		private final Data data;
		private final MessageManager messageManager;
		private final RenderOptions renderOptions;

		private final HelpView helpView = new HelpView();

		private TextRenderer textRenderer;

		public ViewerGLEventListener(Data data, MessageManager messageManager, RenderOptions renderOptions) {
			this.data = data;
			this.messageManager = messageManager;
			this.renderOptions = renderOptions;
		}

		@Override
		public void display(GLAutoDrawable glDrawable) {

	        final GL gl = glDrawable.getGL();

	        AbstractJOGLTarget.clearGL(gl, new Color(0, 0, 0, 0));

	        if (renderOptions.camera == null) {
	        	helpView.renderTo(gl, null, null);
	        } else {

	        	/* prepare projection matrix stack */

	        	//TODO: reactivate to allow
//		        //calculate height for orthographic projection to match
//		        //the height of the perspective view volume at lookAt's distance
//		        double dist = renderOptions.camera.getLookAt().subtract(
//		        		renderOptions.camera.getPos())
//		        		.length();
//		        double tanAngle = Math.tan(renderOptions.projection.getVertAngle());
//		        double height = tanAngle * dist;
//		        renderOptions.projection = renderOptions.projection.withVolumeHeight(height);

	        	/* draw debug views */

	        	DebugView activeWorldObjectView = null;

	        	for (DebugView debugView : renderOptions.activeDebugViews) {
	        		if (debugView instanceof WorldObjectView) {
	        			// needs to be rendered last because of transparency
	        			activeWorldObjectView = debugView;
	        			continue;
	        		}
	        		debugView.renderTo(gl, renderOptions.camera, renderOptions.projection);
	        	}

	        	if (activeWorldObjectView != null) {
	        		activeWorldObjectView.renderTo(gl, renderOptions.camera, renderOptions.projection);
	        	}

	        	/* write messages */

	        	int messageCount = 0;
	        	for (Message message : messageManager.getLiveMessages()) {
	        		textRenderer.drawTextBottom(message.messageString,
	        				10, 10 + messageCount * 20,
	        				Color.WHITE);
	        		messageCount++;
	        	}

	        	gl.glFlush();

	        }

		}

		@Override
		public void init(GLAutoDrawable glDrawable) {
			if ("shader".equals(data.getConfig().getString("joglImplementation"))) {
				textRenderer = new TextRendererShader(glDrawable.getGL().getGL2ES2());
			} else {
				textRenderer = new TextRendererFixedFunction();
			}
			helpView.setConfiguration(data.getConfig());
			//initialization is performed within JOGLTarget
		}

		@Override
		public void reshape(GLAutoDrawable gLDrawable,
				int x, int y, int width, int height) {

			final GL gl = gLDrawable.getGL();

	        if (height <= 0) { // avoid a divide by zero error!
	            height = 1;
	        }

	        gl.glViewport(0, 0, width, height);

	        renderOptions.projection =
	        	renderOptions.projection.withAspectRatio((double)width / height);

	        textRenderer.reshape(width, height);
	        helpView.reshape(width, height);
		}

		@Override
		public void dispose(GLAutoDrawable arg0) {

		}

	}

}
