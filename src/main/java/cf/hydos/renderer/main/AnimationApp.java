package cf.hydos.renderer.main;

import cf.hydos.renderer.renderEngine.RenderEngine;
import cf.hydos.renderer.scene.Scene;
import cf.hydos.renderer.window.Window;

public class AnimationApp {

    /**
     * Initialises the engine and loads the animationrenderer.scene. For every frame it updates the
     * camera, updates the animated entity (which updates the animationrenderer.animation),
     * renders the animationrenderer.scene to the screen, and then updates the display. When the
     * display is close the engine gets cleaned up.
     */
    public static void main(String[] args) {
        RenderEngine engine = RenderEngine.init();

        Scene scene = SceneLoader.loadScene();

        Window.getInstance().run(() -> {
            scene.getCamera().move();
            scene.update();
            engine.renderScene(scene);
            engine.update();
        });

        engine.close();
    }
}
