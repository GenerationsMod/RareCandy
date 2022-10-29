package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.rendering.InstanceState;

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
    private boolean dirty = true;
    private boolean smartRender = false;

    @Override
    public boolean isReady() {
        for (T object : objects) if (!object.isReady()) return false;
        return true;
    }

    public void apply(Consumer<T> consumer) {
        for (T t : objects) if(t.isReady()) consumer.accept(t);
    }

    public void add(T obj) {
        this.objects.add(obj);
        dirty = true;
    }

    @Override
    public void render(List<InstanceState> instances) {
        if (dirty) {
            pipeline = null;
            smartRender = true;
            for (T object : objects) {
                if(pipeline == null) pipeline = object.pipeline;
                else if(pipeline != object.pipeline) smartRender = false;
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
