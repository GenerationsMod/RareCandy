package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.rendering.InstanceState;
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
public class MutiRenderObject<T extends RenderObject> extends RenderObject {

    private final List<T> objects = new ArrayList<>();
    private final List<Consumer<T>> queue = new ArrayList<>();
    private boolean dirty = true;
    private boolean smartRender = false;
    private Matrix4f rootTransformation = new Matrix4f();
    private final Vector3f dimensions = new Vector3f();

    @Override
    public boolean isReady() {
        for (T object : objects) if (!object.isReady()) return false;
        return true;
    }

    public void onUpdate(Consumer<T> consumer) {
        queue.add(consumer);
    }

    public void add(T obj) {
        this.objects.add(obj);
        dirty = true;
        if(obj instanceof MeshObject mesh) {
            dimensions.min(mesh.model.dimensions);
        }
    }

    public void setRootTransformation(Matrix4f rootTransformation) {
        this.rootTransformation = rootTransformation;
    }

    @Override
    public void applyRootTransformation(InstanceState state) {
        state.transformationMatrix().mul(rootTransformation, state.transformationMatrix());
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    @Override
    public void update() {
        for (T t : objects) {
            if (t.isReady()) {
                for (var consumer : queue) {
                    consumer.accept(t);
                }
            }

            t.update();
        }

        queue.clear();
        super.update();
    }

    @Override
    public void render(List<InstanceState> instances) {
        if (dirty) {
            pipeline = null;
            smartRender = true;
            for (T object : objects) {
                if (pipeline == null) pipeline = object.pipeline;
                else if (pipeline != object.pipeline) smartRender = false;
            }
        }

        if (smartRender && isReady()) {
            pipeline.bind();

            for (var instance : instances) {
                pipeline.updateOtherUniforms(instance, objects.get(0));

                for (T object : objects) {
                    if (object instanceof MeshObject meshObject) {
                        pipeline.updateTexUniforms(instance, object);
                        meshObject.model.runDrawCalls();
                    }
                }
            }

            pipeline.unbind();
        } else {
            for (T object : this.objects) {
                object.render(instances);
            }
        }
    }
}
