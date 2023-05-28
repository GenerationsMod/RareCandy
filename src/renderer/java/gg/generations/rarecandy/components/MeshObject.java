package gg.generations.rarecandy.components;

import gg.generations.rarecandy.model.GLModel;
import gg.generations.rarecandy.model.Material;
import gg.generations.rarecandy.model.Variant;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.ObjectInstance;

import java.util.List;
import java.util.Map;

public class MeshObject extends RenderObject {
    public GLModel model;

    public void setup(Variant defaultVariant, Map<String, Variant> variants, GLModel model, Pipeline pipeline) {
        this.defaultVariant = defaultVariant;
        this.variants = variants;
        this.model = model;
        this.pipeline = pipeline;
        this.ready = true;
    }

    public void render(List<ObjectInstance> instances) {
        pipeline.bind();

        for (var instance : instances) {
            if(variants.get(instance.materialId()).hide()) continue;

            pipeline.updateOtherUniforms(instance, this);
            pipeline.updateTexUniforms(instance, this);
            model.runDrawCalls();
        }

        pipeline.unbind();
    }
}
