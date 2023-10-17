package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MeshObject extends RenderObject {
    public GLModel model;
    public String name;

    public void setup(Map<String, Material> variants, List<String> shouldRender, GLModel model, String name) {
        this.name = name;
        this.variants = variants;
        this.shouldRenderList = shouldRender;
        this.model = model;
        this.defaultVariant = new ArrayList<>(variants.keySet()).get(0);
        this.ready = true;
    }

    public <T extends RenderObject> void render(List<ObjectInstance> instances, T object) {
        Map<Material, List<Consumer<Pipeline>>> map = new HashMap<>();

        for (var instance : instances) {
            if (object.shouldRender(instance)) {
                continue;
            }

            var material = object.getMaterial(instance.variant());

            map.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
                pipeline.updateOtherUniforms(instance, object);
                pipeline.updateTexUniforms(instance, object);
                model.runDrawCalls();
            });
        }

        map.forEach((k, v) -> {
            var pl = k.getPipeline();
            pl.bind(k);
            v.forEach(a -> a.accept(pl));
            pl.unbind(k);
        });
    }

    @Override
    public void close() throws IOException {
        super.close();
        model.close();
    }
}
