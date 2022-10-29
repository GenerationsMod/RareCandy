package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.model.GLModel;
import org.joml.Matrix4f;

import java.util.ArrayList;
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
        this.activeAnimation = new ArrayList<>(animations.keySet()).get(0);
        this.ready = true;
    }

    @Override
    public void update() {
        super.update();
        this.boneTransforms = animations.get(activeAnimation).getFrameTransform(animationTime);
    }
}
