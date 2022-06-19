package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Used if a .glb has multiple models inside it
 */
public class ListRenderObject extends RenderObject {
    private final List<RenderObject> renderObjects = new ArrayList<>();

    @Override
    public void render(Matrix4f projectionMatrix, List<InstanceState> instances) {
        for (RenderObject renderObject : this.renderObjects) {
            renderObject.render(projectionMatrix, instances);
        }
    }

    public void add(RenderObject object) {
        this.renderObjects.add(object);
    }

    public void remove(RenderObject object) {
        this.renderObjects.remove(object);
    }
}
