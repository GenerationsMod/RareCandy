package cf.hydos;

import cf.hydos.animationRendering.animation.AnimationUtil;
import cf.hydos.animationRendering.engine.core.Game;
import cf.hydos.animationRendering.engine.core.GameObject;
import org.joml.Vector3f;

import java.io.File;

public class Test extends Game {
    public void Init() {
        GameObject animatedObject = new GameObject().AddComponent(AnimationUtil.loadAnimatedFile(new File("/venusaur.glb")));
        animatedObject.GetTransform()
                .scale(new Vector3f(4f, 4f, 4f))
                .translate(new Vector3f(-20, -8, -40))
                .rotate((float) Math.toRadians(90), new Vector3f(-1, 0, 0));
        AddObject(animatedObject);
    }
}
