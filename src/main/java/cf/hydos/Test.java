package cf.hydos;

import cf.hydos.animationRendering.animation.AnimationUtil;
import cf.hydos.animationRendering.engine.components.Camera;
import cf.hydos.animationRendering.engine.core.Game;
import cf.hydos.animationRendering.engine.core.GameObject;
import cf.hydos.animationRendering.engine.rendering.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;

public class Test extends Game {
    public void Init() {
        AddObject(new GameObject().AddComponent(new Camera(new Matrix4f().perspective((float) Math.toRadians(45), (float) Window.GetWidth() / Window.GetHeight(), 0.1f, 1000.0f))));

        GameObject animatedObject = new GameObject().AddComponent(AnimationUtil.loadAnimatedFile(new File("/venusaur.glb")));
        animatedObject.GetTransform()
                .scale(new Vector3f(0.4f, 0.4f, 0.4f))
                .translate(new Vector3f(-20, -2, -40))
                .rotate((float) Math.toRadians(90), new Vector3f(-1, 0, 0));
        AddObject(animatedObject);
    }
}
