package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stores multiple separate render objects of the same type into one {@link RenderObject}
 *
 * @param <T> the type to use
 */
public class MultiRenderObject<T extends RenderObject> extends RenderObject {

    public final List<T> objects = new ArrayList<>();
    private final List<Consumer<T>> queue = new ArrayList<>();
    private final boolean smartRender = false;
    private final Vector3f dimensions = new Vector3f();
    public float scale = 1.0f;
    private boolean dirty = true;
    private Matrix4f rootTransformation = new Matrix4f();

    public MultiRenderObject() {
        variants = new HashMap<>();
    }

    public void onUpdate(Consumer<T> consumer) {
        queue.add(consumer);
    }

    public void add(T obj) {
        this.objects.add(obj);
        dirty = true;
    }

    public Matrix4f getRootTransformation() {
        return rootTransformation;
    }

    public void setRootTransformation(Matrix4f rootTransformation) {
        this.rootTransformation = rootTransformation;
    }

    public void applyRootTransformation(ObjectInstance state) {
        state.transformationMatrix().mul(rootTransformation, state.transformationMatrix());
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    @Override
    public void update() {
        for (T t : objects) {
            t.update();
        }

        if (objects.get(0) != null && objects.get(0).isReady()) {
            for (var consumer : queue) {
                consumer.accept(objects.get(0));
            }
        }

        queue.clear();
        super.update();
    }

    @Override
    public boolean isReady() {
        if (objects.isEmpty()) return false;
        for (T object : objects) if (!object.isReady()) return false;
        return true;
    }

    @Override
    public Set<String> availableVariants() {
        return objects.get(0).availableVariants();
    }

    @Override
    public Material getVariant(@Nullable String materialId) {
        return objects.get(0).getVariant(materialId);
    }

    @Override
    public <V extends RenderObject> void render(Predicate<Material> predicate, List<ObjectInstance> instances, V obj) {
//        if (dirty) {
//            pipeline = null;
//            smartRender = true;
//            for (T object : objects) {
//                if (pipeline == null) pipeline = object..pipeline;
//                else if (pipeline != object.pipeline) smartRender = false;
//            }
//        }
//
//        if (smartRender && isReady()) {
//            Map<String, List<Consumer<Pipeline>>> map = new HashMap<>();
//
//            for (var instance : instances) {
//                var material = objects.get(0).getMaterial(instance.variant()).getType();
//
//                var entry = map.computeIfAbsent(material, a -> new ArrayList<>());
//
//                entry.add(pipeline -> {
//                    pipeline.updateOtherUniforms(instance, this);
//
//                    for (T object : objects) {
//                        if (object instanceof MeshObject meshObject) {
//                            if (meshObject.getVariant(instance.materialId()).hide()) continue;
//                            pipeline.updateTexUniforms(instance, meshObject);
//                            meshObject.model.runDrawCalls();
//                        }
//                    }
//                });
//            }
//
//            map.forEach((k, v) -> {
//                var pl = pipeline.apply(k);
//                pl.bind();
//                v.forEach(a -> a.accept(pl));
//                pl.unbind();
//            });
//        } else {
        for (T object : this.objects) {
            object.render(predicate, instances, object);
        }
//        }
    }

    public void updateDimensions() {
        for (RenderObject object : objects) {
            if (object instanceof MeshObject mesh) {
                dimensions.max(mesh.model.dimensions);
            }
        }
    }
}
