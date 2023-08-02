package gg.generations.rarecandy.components;

import gg.generations.rarecandy.animation.Animation;
import gg.generations.rarecandy.animation.Skeleton;
import gg.generations.rarecandy.model.GLModel;
import gg.generations.rarecandy.model.material.Material;
import gg.generations.rarecandy.pipeline.Pipeline;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AnimatedMeshObject extends MeshObject {
    public Map<String, Animation> animations;
    public Skeleton skeleton;

    public void setup(Map<String, Material> variants, List<String> shouldRender, GLModel model, Function<String, Pipeline> pipeline, String name, Skeleton skeleton, Map<String, Animation> animations) {
        setup(variants, shouldRender, model, pipeline, name);
        this.animations = animations;
        this.skeleton = skeleton;
    }
}
