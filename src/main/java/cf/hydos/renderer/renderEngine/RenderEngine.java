package cf.hydos.renderer.renderEngine;

import cf.hydos.renderer.renderer.AnimatedModelRenderer;
import cf.hydos.renderer.scene.Scene;
import cf.hydos.renderer.skybox.SkyboxRenderer;
import cf.hydos.renderer.utils.DisplayManager;

/**
 * This class represents the entire render engine.
 */
public class RenderEngine {

    private final MasterRenderer renderer;

    private RenderEngine(MasterRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Updates the display.
     */
    public void update() {
        DisplayManager.update();
    }

    /**
     * Renders the animationrenderer.scene to the screen.
     *
     * @param scene - the game animationrenderer.scene.
     */
    public void renderScene(Scene scene) {
        renderer.renderScene(scene);
    }

    /**
     * Cleans up the renderers and closes the display.
     */
    public void close() {
        renderer.cleanUp();
        DisplayManager.closeDisplay();
    }

    /**
     * Initializes a new render engine. Creates the display and inits the
     * renderers.
     *
     * @return
     */
    public static RenderEngine init() {
        DisplayManager.createDisplay();
        SkyboxRenderer skyRenderer = new SkyboxRenderer();
        AnimatedModelRenderer entityRenderer = new AnimatedModelRenderer();
        MasterRenderer renderer = new MasterRenderer(entityRenderer, skyRenderer);
        return new RenderEngine(renderer);
    }

}
