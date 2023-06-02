package gg.generations.rarecandy.components;

import gg.generations.rarecandy.model.GLModel;
import gg.generations.rarecandy.model.Material;
import gg.generations.rarecandy.model.Variant;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.ObjectInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MeshObject extends RenderObject {
    public GLModel model;
    public String name;

    public void setup(Variant defaultVariant, Map<String, Variant> variants, GLModel model, Function<String, Pipeline> pipeline, String name) {
        this.name = name;
        this.defaultVariant = defaultVariant;
        this.variants = variants;
        this.model = model;
        this.pipeline = pipeline;
        this.ready = true;
    }

    public <T extends RenderObject> void render(List<ObjectInstance> instances, T object) {
        Map<String, List<Consumer<Pipeline>>> map = new HashMap<>();

        for (var instance : instances) {
            if(object.getVariant(instance.materialId()).hide()) {
                continue;
            }

            var material = object.getMaterial(instance.variant()).getType();

            map.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
                pipeline.updateOtherUniforms(instance, object);
                pipeline.updateTexUniforms(instance, object);
                model.runDrawCalls();
            });
        }

        map.forEach((k, v) -> {
            var pl = pipeline.apply(k);
            pl.bind();
            v.forEach(a -> a.accept(pl));
            pl.unbind();
        });
    }
}
