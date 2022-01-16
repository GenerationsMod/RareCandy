package cf.hydos;

import cf.hydos.engine.animation.AnimationUtil;
import cf.hydos.engine.core.Renderer;
import cf.hydos.engine.core.RenderingApplication;
import cf.hydos.engine.core.RenderObject;
import org.joml.Vector3f;

import java.io.File;

public class Main extends RenderingApplication {

    public static void main(String[] args) {
        Renderer renderer = new Renderer(1920, 1080, 1200, new Main());
        renderer.title("Pixelmon: Generations Test renderer");
        renderer.start();
    }

    @Override
    public void init() {
        RenderObject venusaur = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(new File("/venusaur.glb")));
        venusaur.getTransformation()
                .scale(new Vector3f(4f, 4f, 4f))
                .translate(new Vector3f(-20, -8, -40))
                .rotate((float) Math.toRadians(90), new Vector3f(-1, 0, 0));
        add(venusaur);
    }
}
