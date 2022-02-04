package cf.hydos.engine.core;

import cf.hydos.engine.components.GameComponent;
import cf.hydos.engine.rendering.RenderingEngine;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class RenderObject {
    private final ArrayList<RenderObject> children;
    private final ArrayList<GameComponent> components;
    private final Matrix4f transform;
    private LoopManager engine;

    public RenderObject() {
        children = new ArrayList<>();
        components = new ArrayList<>();
        transform = new Matrix4f();
        engine = null;
    }

    public void addChild(RenderObject child) {
        children.add(child);
        child.SetEngine(engine);
        child.getTransformation().mul(transform);
    }

    public RenderObject addComponent(GameComponent component) {
        components.add(component);
        component.SetParent(this);

        return this;
    }

    public void update() {
        for (RenderObject child : children)
            child.update();
    }

    public void render(RenderingEngine renderingEngine) {
        renderComponents(renderingEngine);

        for (RenderObject child : children)
            child.render(renderingEngine);
    }

    public void renderComponents(RenderingEngine renderingEngine) {
        for (GameComponent component : components)
            component.Render(new Matrix4f(renderingEngine.projViewMatrix));
    }

    public Matrix4f getTransformation() {
        return transform;
    }

    public void SetEngine(LoopManager engine) {
        if (this.engine != engine) {
            this.engine = engine;

            for (RenderObject child : children)
                child.SetEngine(engine);
        }
    }
}
