package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.model.RenderModel;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeshObject extends RenderObject {
    public RenderModel model;
    public String name;

    public void setup(Map<String, Variant> variants, RenderModel model, String name) {
        this.name = name;
        this.variants = variants;
        this.model = model;
        this.defaultVariant = new ArrayList<>(variants.keySet()).get(0);
        this.ready = true;
    }

    public <T extends RenderObject> void render(List<ObjectInstance> instances) {
        model.render(instances, this);
    }

    public <T extends RenderObject> void render(ObjectInstance instance) {
        model.render(instance, this);
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
