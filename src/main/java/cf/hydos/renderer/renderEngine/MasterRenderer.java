package cf.hydos.renderer.renderEngine;

import cf.hydos.renderer.renderer.AnimatedModelRenderer;
import cf.hydos.renderer.scene.Scene;
import cf.hydos.renderer.skybox.SkyboxRenderer;
import org.lwjgl.opengl.GL11;

/**
 * This class is in charge of rendering everything in the com.thinmatrix.animationrenderer.scene to the screen.
 * @author Karl
 *
 */
public class MasterRenderer {

	private SkyboxRenderer skyRenderer;
	private AnimatedModelRenderer entityRenderer;

	protected MasterRenderer(AnimatedModelRenderer renderer, SkyboxRenderer skyRenderer) {
		this.skyRenderer = skyRenderer;
		this.entityRenderer = renderer;
	}

	/**
	 * Renders the com.thinmatrix.animationrenderer.scene to the screen.
	 * @param scene
	 */
	protected void renderScene(Scene scene) {
		prepare();
		entityRenderer.render(scene.getAnimatedModel(), scene.getCamera(), scene.getLightDirection());
		skyRenderer.render(scene.getCamera());
	}

	/**
	 * Clean up when the game is closed.
	 */
	protected void cleanUp() {
		skyRenderer.cleanUp();
		entityRenderer.cleanUp();
	}

	/**
	 * Prepare to render the current frame by clearing the framebuffer.
	 */
	private void prepare() {
		GL11.glClearColor(1, 1, 1, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}


}
