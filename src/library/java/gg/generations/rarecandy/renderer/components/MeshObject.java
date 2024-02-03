package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MeshObject extends RenderObject {
    public GLModel model;
    public String name;

    public void setup(Map<String, Material> variants, List<String> shouldRender, GLModel model, String name) {
        this.name = name;
        this.variants = variants;
        variants.forEach(new BiConsumer<String, Material>() {
            @Override
            public void accept(String s, Material material) {
                if(material.blendType() != BlendType.None) {
                    MeshObject.this.transparentList.add(s);
                }
            }
        });
        this.shouldRenderList = shouldRender;
        this.model = model;
        this.defaultVariant = new ArrayList<>(variants.keySet()).get(0);
        this.ready = true;
    }

    public <T extends RenderObject> void render(ObjectInstance instance, T object) {
        Map<Material, List<Consumer<Pipeline>>> map = new HashMap<>();

        if (object.shouldRender(instance)) {
            return;
        }

        var material = object.getMaterial(instance.variant());

        map.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
                pipeline.updateOtherUniforms(instance, object);
                pipeline.updateTexUniforms(instance, object);
                model.runDrawCalls();
        });

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

    @Override
    public String toString() {
        return name;
    }
}
