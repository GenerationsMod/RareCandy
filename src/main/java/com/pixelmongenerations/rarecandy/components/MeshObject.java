package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import me.hydos.gogoat.GLModel;

import java.util.List;
import java.util.Map;

public class MeshObject extends RenderObject {
    protected GLModel glModel;

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
