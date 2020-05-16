package org.osm2world.core.target.jogl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import org.osm2world.core.target.common.TextureData;

import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * loads textures from files to JOGL and keeps them available for future use
 */
public class JOGLTextureManager {

	private final GL gl;

	private final Map<File, Texture> availableTextures = new HashMap<File, Texture>();

	public JOGLTextureManager(GL gl) {
		this.gl = gl;
	}

	public Texture getTextureForFile(File file) {
		return getTextureForFile(file, true);
	}

	public Texture getTextureForTextureData(TextureData textureData) {
		return getTextureForFile(textureData.getRasterImage());
	}

	public Texture getTextureForFile(File file, boolean createMipmaps) {

		Texture result = availableTextures.get(file);

		if (result == null) {

			synchronized (this) {

				//try again

				if (availableTextures.containsKey(file)) {
					return availableTextures.get(file);
				}

				try {

					if (!file.getName().toLowerCase().endsWith("png")) {

						//flip to ensure consistent tex coords with png images
						BufferedImage bufferedImage = ImageIO.read(file);
						ImageUtil.flipImageVertically(bufferedImage);

						result = AWTTextureIO.newTexture(
								gl.getGLProfile(), bufferedImage, createMipmaps);

					} else {

						result = TextureIO.newTexture(file, createMipmaps);

					}

					/* workaround for OpenGL 3: call to glGenerateMipmap is missing in [AWT]TextureIO.newTexture()
					 * May be fixed in new versions of JOGL.
					 */
					if (createMipmaps && gl.isGL3()) {
						gl.glGenerateMipmap(result.getTarget());
					}

					availableTextures.put(file, result);

				} catch (IOException exc) {

					exc.printStackTrace();
					System.exit(2);
					//TODO error handling

				}
			}

		}

		return result;

	}


	public void releaseAll() {

		for (Texture texture : availableTextures.values()) {
			texture.destroy(gl);
		}

		availableTextures.clear();

	}

}

