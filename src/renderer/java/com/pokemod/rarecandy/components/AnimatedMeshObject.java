package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.animation.Skeleton;
import com.pokemod.rarecandy.model.GLModel;
import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.pipeline.Pipeline;

import java.util.List;
import java.util.Map;

public class AnimatedMeshObject extends MeshObject {
    public Map<String, Animation> animations;
    public Skeleton skeleton;

    public void setup(List<Material> glMaterials, Map<String, Material> variants, GLModel model, Pipeline pipeline, Skeleton skeleton, Map<String, Animation> animations) {
        this.materials = glMaterials;
        this.variants = variants;
        this.model = model;
        this.pipeline = pipeline;
        this.animations = animations;
        this.ready = true;
    }
}
