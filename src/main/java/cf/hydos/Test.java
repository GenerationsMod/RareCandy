package cf.hydos;

import cf.hydos.animationRendering.animation.AnimationUtil;
import cf.hydos.animationRendering.engine.components.Camera;
import cf.hydos.animationRendering.engine.components.FreeLook;
import cf.hydos.animationRendering.engine.components.FreeMove;
import cf.hydos.animationRendering.engine.core.*;
import cf.hydos.animationRendering.engine.rendering.Window;

import java.io.File;

public class Test extends Game {
    public void Init() {
        AddObject(
                //AddObject(
                new GameObject().AddComponent(new FreeLook(0.3f)).AddComponent(new FreeMove(10.0f))
                        .AddComponent(new Camera(new Matrix4f().InitPerspective((float) Math.toRadians(90.0f),
                                (float) Window.GetWidth() / (float) Window.GetHeight(), 0.01f, 1000.0f))));

        GameObject animatedObject = new GameObject().AddComponent(AnimationUtil.loadAnimatedFile(new File("/venusaur.glb")));
        animatedObject.GetTransform().SetRot(new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(-90)));
        animatedObject.GetTransform().SetScale(new Vector3f(0.4f, 0.4f, 0.4f));
        AddObject(animatedObject);
    }
}
