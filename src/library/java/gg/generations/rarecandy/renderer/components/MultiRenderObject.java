package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Stores multiple separate render objects of the same type into one {@link RenderObject}
 *
 * @param <T> the type to use
 */
public class MultiRenderObject<T extends RenderObject> extends RenderObject {

    public final List<T> objects = new CopyOnWriteArrayList<>();
    private final List<Consumer<T>> queue = new ArrayList<>();
    private final boolean smartRender = false;
    public final Vector3f dimensions = new Vector3f();
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
            if (t != null) t.update();
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
    public <V extends RenderObject> void render(RenderStage stage, List<ObjectInstance> instances, V obj) {
        for (T object : this.objects) {
            if (object != null && object.isReady()) {
                object.render(stage, instances, object);
            }
        }
    }

    public void updateDimensions() {
        for (RenderObject object : objects) {
            if (object instanceof MeshObject mesh) {
                dimensions.max(mesh.model.dimensions);
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (T object : objects) {
            object.close();
        }
    }
}
