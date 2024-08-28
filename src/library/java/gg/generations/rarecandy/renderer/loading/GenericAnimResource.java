package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;

import java.util.HashMap;
import java.util.Map;

public record GenericAnimResource(long fps, Animation.AnimationNode[] nodes) implements AnimResource {
    @Override
    public Animation.AnimationNode[] getNodes(Skeleton skeleton) {
        return nodes;
    }

    @Override
    public Map<String, Animation.Offset> getOffsets() {
        return new HashMap<>();
    }
}
