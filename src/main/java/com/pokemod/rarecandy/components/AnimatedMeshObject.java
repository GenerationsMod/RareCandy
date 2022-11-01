package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.model.GLModel;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AnimatedMeshObject extends MeshObject {
    public Map<String, Animation> animations;
    public Matrix4f[] boneTransforms;
    public String activeAnimation;
    public double animationTime;

    public void setup(List<Material> glMaterials, Map<String, Material> variants, GLModel model, Pipeline pipeline, Map<String, Animation> animations) {
        this.materials = glMaterials;
        this.variants = variants;
        this.model = model;
        this.pipeline = pipeline;
        this.animations = animations;
        this.boneTransforms = new Matrix4f[0];
        this.activeAnimation = null;
        this.ready = true;
    }

    @Override
    public void update() {
        super.update();
        if(activeAnimation == null) {
            boneTransforms = new Matrix4f[200];
            var identity = new Matrix4f().identity();
            Arrays.fill(boneTransforms, identity);
        }
        else this.boneTransforms = animations.get(activeAnimation).getFrameTransform(animationTime);
    }

    public Animation getAnimation(String activeAnimation) {
        return animations.get(activeAnimation);
    }
}
