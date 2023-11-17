package gg.generations.rarecandy.renderer.animation;

import org.joml.Vector2f;

public class GfbAnimationInstance extends AnimationInstance {
    private Vector2f eyeOffset;

    public GfbAnimationInstance(GfbAnimation animation) {
        super(animation);
    }

    @Override
    public void update(double secondsPassed) {
        super.update(secondsPassed);

        eyeOffset = calcEyeOffset();
    }

    private Vector2f calcEyeOffset() {
        var u = calcInterpolatedFloat(currentTime, ((GfbAnimation) animation).eyeOffsetU);
        var v = calcInterpolatedFloat(currentTime, ((GfbAnimation) animation).eyeOffsetV);

        return new Vector2f(u, v);
    }

    public static Float calcInterpolatedFloat(float animTime, TransformStorage<Float> node) {
        if (node.size() == 1) return 0.0f;

        var offset = findOffset(animTime, node);
        return offset.value();
    }

    public static TransformStorage.TimeKey<Float> findOffset(float animTime, TransformStorage<Float> keys) {
        for (var key : keys) {
            if (animTime < key.time())
                return keys.getBefore(key);
        }

        return keys.get(0);
    }

    public Vector2f getEyeOffset() {
        return eyeOffset;
    }
}
