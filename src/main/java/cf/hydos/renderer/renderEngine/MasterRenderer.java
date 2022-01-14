package cf.hydos.renderer.renderEngine;

import cf.hydos.renderer.animatedModel.AnimatedModel;
import cf.hydos.renderer.renderer.AnimatedModelRenderer;
import cf.hydos.renderer.scene.Scene;
import cf.hydos.renderer.skybox.SkyboxRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * This class is in charge of rendering everything in the com.thinmatrix.animationrenderer.scene to the screen.
 */
public class MasterRenderer {

    private final SkyboxRenderer skyRenderer;
    private final AnimatedModelRenderer entityRenderer;

    protected MasterRenderer(AnimatedModelRenderer renderer, SkyboxRenderer skyRenderer) {
        this.skyRenderer = skyRenderer;
        this.entityRenderer = renderer;
    }

    /**
     * Renders the com.thinmatrix.animationrenderer.scene to the screen.
     *
     * @param scene
     */
    protected void renderScene(Scene scene) {
        prepare();
        Matrix4f projMatrix = scene.getCamera().getProjectionViewMatrix();
        for (AnimatedModel model : scene.models) {
            entityRenderer.render(model, projMatrix, scene.getLightDirection());
            projMatrix.translate(60, 0, 0);
        }
        skyRenderer.render(scene.getCamera());
    }

    /**
     * Clean up when the game is closed.
     */
    protected void cleanUp() {
        entityRenderer.cleanUp();
        skyRenderer.cleanUp();
    }

    /**
     * Prepare to render the current frame by clearing the framebuffer.
     */
    private void prepare() {
        GL11.glClearColor(1, 1, 1, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }


}
