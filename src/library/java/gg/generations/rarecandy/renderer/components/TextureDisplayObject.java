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

public class TextureDisplayObject extends RenderObject {
    private Map<Material, List<Consumer<Pipeline>>> EMPTY = Collections.emptyMap();
    public GLModel model;
    public String name;

    private static void render(Material k, List<Consumer<Pipeline>> v) {
        var pl = k.getPipeline();

        pl.bind(k);
        v.forEach(a -> a.accept(pl));
        pl.unbind(k);
    }

    public void setup(GLModel model, String name) {
        this.name = name;
        this.shouldRenderList = new ArrayList<>();
        this.offsets = new HashMap<>();
        this.model = model;
    }

    public void setMaterials(Map<String, Material> materials) {
        if(materials != null && !materials.isEmpty()) {
            this.variants = materials;
            this.defaultVariant = new ArrayList<>(variants.keySet()).get(0);
            this.ready = true;
        } else {
            this.variants = null;
            this.defaultVariant = null;
            this.ready = false;
        }
    }

    public <T extends RenderObject> void render(List<ObjectInstance> instances, T object) {
        Map<Material, List<Consumer<Pipeline>>> map = new HashMap<>();

        for (var instance : instances) {

            var material = object.getMaterial(instance.variant());

            map.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
                pipeline.updateOtherUniforms(instance, object);
                pipeline.updateTexUniforms(instance, object);
                model.runDrawCalls();
            });
        }

        map.forEach(TextureDisplayObject::render);
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
