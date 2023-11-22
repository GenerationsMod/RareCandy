package gg.generations.rarecandy.renderer.animation;

import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class GfbAnimationInstance extends AnimationInstance {
    private final Map<String, Vector2f> eyeOffset = new HashMap<>();

    public GfbAnimationInstance(GfbAnimation animation) {
        super(animation);

        animation.offsets.keySet().forEach(k -> eyeOffset.put(k, new Vector2f()));
    }

    @Override
    public void update(double secondsPassed) {
        super.update(secondsPassed);

        ((GfbAnimation) animation).offsets.forEach((k, v) -> v.calcOffset(currentTime, eyeOffset.get(k)));
    }

    public Vector2f getEyeOffset(String name) {
        var offset = eyeOffset.get(name.replaceFirst("shiny_", "")/* Correction factor for now converted swsh models. TODO: More elegant solution.*/);

        if(offset == null) {
            return AnimationController.NO_OFFSET;
        } else {
            return offset;
        }
    }
}
