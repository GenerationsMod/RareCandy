package gg.generations.rarecandy.components;

import gg.generations.pokeutils.ModelConfig;
import gg.generations.rarecandy.animation.Animation;
import gg.generations.rarecandy.animation.Skeleton;
import gg.generations.rarecandy.model.GLModel;
import gg.generations.rarecandy.model.material.Material;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.ObjectInstance;
import gg.generations.rarecandy.storage.AnimatedObjectInstance;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AnimatedMeshObject extends MeshObject {
    public Map<String, Animation> animations;
    public Skeleton skeleton;
    private ModelConfig.HideDuringAnimation hideDuringAnimation;

    public void setup(Map<String, Material> variants, List<String> shouldRender, GLModel model, Function<String, Pipeline> pipeline, String name, Skeleton skeleton, Map<String, Animation> animations, ModelConfig.HideDuringAnimation hideDuringAnimation) {
        setup(variants, shouldRender, model, pipeline, name);
        this.hideDuringAnimation = hideDuringAnimation;
        this.animations = animations;
        this.skeleton = skeleton;
    }

    @Override
    protected boolean shouldRender(ObjectInstance instance) {
        return super.shouldRender(instance) ||
                (instance instanceof AnimatedObjectInstance animationInstance && animationInstance.currentAnimation != null && hideDuringAnimation.check(animationInstance.currentAnimation.getAnimation().name));
    }
}
