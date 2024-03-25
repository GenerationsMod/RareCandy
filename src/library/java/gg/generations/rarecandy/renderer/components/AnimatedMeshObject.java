package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;

public class AnimatedMeshObject extends MeshObject {
    public Map<String, Animation> animations;
    public Skeleton skeleton;
    private ModelConfig.HideDuringAnimation hideDuringAnimation;

    public void setup(Map<String, Material> variants, List<String> shouldRender, Map<String, Vector2f> offset, GLModel model, String name, Skeleton skeleton, Map<String, Animation> animations, ModelConfig.HideDuringAnimation hideDuringAnimation) {
        setup(variants, shouldRender, offset, model, name);
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
