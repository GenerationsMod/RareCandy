package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stores multiple separate render objects of the same type into one {@link RenderObject}
 *
 * @param <T> the type to use
 */
public class MultiRenderObject<T extends RenderObject> extends RenderObject {

    public float scale;
    public final List<T> hiddenObjects = new ArrayList<>();
    public final List<T> objects = new ArrayList<>();
    private final List<Consumer<T>> queue = new ArrayList<>();
    private boolean dirty = true;
    private boolean smartRender = false;
    private Matrix4f rootTransformation = new Matrix4f();
    private final Vector3f dimensions = new Vector3f();

    @Override
    public boolean isReady() {
        if (objects.isEmpty()) return false;
        for (T object : objects) if (!object.isReady()) return false;
        return true;
    }

    public void onUpdate(Consumer<T> consumer) {
        queue.add(consumer);
    }

    public void add(T obj, boolean hidden) {
        if(hidden) hiddenObjects.add(obj);
        else objects.add(obj);
        dirty = true;
    }

    public void unhideAll() {
        objects.addAll(hiddenObjects);
        hiddenObjects.clear();;
    }

    public void setRootTransformation(Matrix4f rootTransformation) {
        this.rootTransformation = rootTransformation;
    }

    public Matrix4f getRootTransformation() {
        return rootTransformation;
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
    public void render(List<ObjectInstance> instances) {
        if (dirty) {
            shaderPipeline = null;
            smartRender = true;
            for (T object : objects) {
                if (shaderPipeline == null) shaderPipeline = object.shaderPipeline;
                else if (shaderPipeline != object.shaderPipeline) smartRender = false;
            }
        }

        if (smartRender && isReady()) {
            shaderPipeline.bind();

            for (var instance : instances) {
                shaderPipeline.updateOtherUniforms(instance, objects.get(0));

                for (T object : objects) {
                    if (object instanceof MeshObject meshObject) {
                        shaderPipeline.updateTexUniforms(instance, object);
                        meshObject.model.runDrawCalls();
                    }
                }
            }

            shaderPipeline.unbind();
        } else {
            for (T object : this.objects) {
                object.render(instances);
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
}
