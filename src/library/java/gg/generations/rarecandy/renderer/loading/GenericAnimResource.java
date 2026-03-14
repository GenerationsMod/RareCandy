package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.renderer.animation.Animation;

import java.util.HashMap;
import java.util.Map;

public record GenericAnimResource(long fps, boolean loops, Map<String, Animation.AnimationNode> nodes) implements AnimResource {
    @Override
    public Map<String, Animation.AnimationNode> getNodes() {
        return nodes;
    }

    @Override
    public Map<String, Animation.Offset> getOffsets() {
        return new HashMap<>();
    }
}
