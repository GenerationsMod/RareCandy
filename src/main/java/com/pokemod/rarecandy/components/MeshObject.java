package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.model.Material;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.InstanceState;
import com.pokemod.rarecandy.model.GLModel;

import java.util.List;
import java.util.Map;

public class MeshObject extends RenderObject {
    public GLModel glModel;

    public void setup(List<Material> glMaterials, Map<String, Material> variants, GLModel glModel, Pipeline pipeline) {
        this.materials = glMaterials;
        this.variants = variants;
        this.glModel = glModel;
        this.pipeline = pipeline;
        this.ready = true;
    }

    public void render(List<InstanceState> instances) {
        pipeline.bind();
        for (var instance : instances) {
            pipeline.updateUniforms(instance, this);
            glModel.runDrawCalls();
        }
        pipeline.unbind();
    }
}
