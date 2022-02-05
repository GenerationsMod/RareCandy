package cf.hydos.engine.core;

import cf.hydos.engine.components.GameComponent;
import cf.hydos.engine.rendering.RenderingEngine;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class RenderObject {
    private final ArrayList<RenderObject> children;
    private final ArrayList<GameComponent> components;
    private final Matrix4f transformMatrix;

    public RenderObject() {
        children = new ArrayList<>();
        components = new ArrayList<>();
        transformMatrix = new Matrix4f();
    }

    public void addChild(RenderObject child) {
        children.add(child);
        child.getTransformation().mul(transformMatrix);
    }

    public RenderObject addComponent(GameComponent component) {
        components.add(component);
        component.SetParent(this);

        return this;
    }

    public void update() {
        for (RenderObject child : children)
            child.update();

        for (GameComponent component : components) {
            component.update();
        }
    }

    public void render(RenderingEngine renderingEngine) {
        renderComponents(renderingEngine);

        for (RenderObject child : children)
            child.render(renderingEngine);
    }

    public void renderComponents(RenderingEngine renderingEngine) {
        for (GameComponent component : components)
            component.render(renderingEngine.projectionMatrix, renderingEngine.viewMatrix);
    }

    public Matrix4f getTransformation() {
        return transformMatrix;
    }
}
