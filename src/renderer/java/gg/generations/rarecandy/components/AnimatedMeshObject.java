package gg.generations.rarecandy.components;

import gg.generations.rarecandy.animation.Animation;
import gg.generations.rarecandy.animation.Skeleton;
import gg.generations.rarecandy.model.GLModel;
import gg.generations.rarecandy.model.Material;
import gg.generations.rarecandy.pipeline.Pipeline;

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
