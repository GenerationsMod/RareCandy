package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class MeshObject extends RenderObject {
    private Map<Material, List<Consumer<Pipeline>>> EMPTY = Collections.emptyMap();
    public GLModel model;
    public String name;

    private static void render(Material k, List<Consumer<Pipeline>> v) {
        var pl = k.getPipeline();

        if(pl == null) return;

        pl.bind(k);
        v.forEach(a -> a.accept(pl));
        pl.unbind(k);
    }

    public void setup(Map<String, Material> variants, List<String> shouldRender, Map<String, Vector2f> offset, GLModel model, String name) {
        this.name = name;
        this.variants = variants;
        this.shouldRenderList = shouldRender;
        this.offsets = offset;
        this.model = model;
        this.defaultVariant = new ArrayList<>(variants.keySet()).get(0);
        this.ready = true;
    }

    public <T extends RenderObject> void render(List<ObjectInstance> instances, T object) {
        Map<RenderStage, Map<Material, List<Consumer<Pipeline>>>> map = new HashMap<>();

        for (var instance : instances) {
            if (object.shouldRender(instance)) {
                continue;
            }

            var material = object.getMaterial(instance.variant());

            var stage = RenderStage.SOLID;

            if(material.blendType() != BlendType.None) stage = RenderStage.TRANSPARENT;

            var stages = map.computeIfAbsent(stage, s -> new HashMap<>());

            stages.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
                pipeline.updateOtherUniforms(instance, object);
                pipeline.updateTexUniforms(instance, object);
                model.runDrawCalls();
            });
        }



        map.getOrDefault(RenderStage.SOLID, EMPTY).forEach(MeshObject::render);
        map.getOrDefault(RenderStage.TRANSPARENT, EMPTY).forEach(MeshObject::render);
    }

    public <T extends RenderObject> void render(ObjectInstance instance, T object) {
        Map<Material, List<Consumer<Pipeline>>> solidMap = new HashMap<>();
        Map<Material, List<Consumer<Pipeline>>> transparentMap = new HashMap<>();

        if (object.shouldRender(instance)) return;

        var material = object.getMaterial(instance.variant());

        var stage = RenderStage.SOLID;

        if(material.blendType() != BlendType.None) stage = RenderStage.TRANSPARENT;
        var stages = stage == RenderStage.SOLID ? solidMap : transparentMap;

        stages.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
            pipeline.updateOtherUniforms(instance, object);
            pipeline.updateTexUniforms(instance, object);
            model.runDrawCalls();
        });

        solidMap.forEach(MeshObject::render);
        transparentMap.forEach(MeshObject::render);
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
