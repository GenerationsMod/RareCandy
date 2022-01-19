package cf.hydos.engine.core;

import cf.hydos.engine.components.GameComponent;
import cf.hydos.engine.rendering.RenderingEngine;
import cf.hydos.engine.rendering.Shader;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class RenderObject {
    private final ArrayList<RenderObject> children;
    private final ArrayList<GameComponent> components;
    private final Matrix4f transform;
    private Renderer engine;

    public RenderObject() {
        children = new ArrayList<>();
        components = new ArrayList<>();
        transform = new Matrix4f();
        engine = null;
    }

    public RenderObject AddChild(RenderObject child) {
        children.add(child);
        child.SetEngine(engine);
        child.getTransformation().mul(transform);

        return this;
    }

    public RenderObject addComponent(GameComponent component) {
        components.add(component);
        component.SetParent(this);

        return this;
    }

    public void InputAll(float delta) {
        Input(delta);

        for (RenderObject child : children)
            child.InputAll(delta);
    }

    public void UpdateAll(float delta) {
        Update(delta);

        for (RenderObject child : children)
            child.UpdateAll(delta);
    }

    public void RenderAll(Shader shader, RenderingEngine renderingEngine) {
        Render(shader, renderingEngine);

        for (RenderObject child : children)
            child.RenderAll(shader, renderingEngine);
    }

    public void Input(float delta) {
        for (GameComponent component : components)
            component.Input(delta);
    }

    public void Update(float delta) {
        for (GameComponent component : components)
            component.Update(delta);
    }

    public void Render(Shader shader, RenderingEngine renderingEngine) {
        for (GameComponent component : components)
            component.Render(shader, renderingEngine);
    }

    public ArrayList<RenderObject> GetAllAttached() {
        ArrayList<RenderObject> result = new ArrayList<>();

        for (RenderObject child : children)
            result.addAll(child.GetAllAttached());

        result.add(this);
        return result;
    }

    public Matrix4f getTransformation() {
        return transform;
    }

    public void SetEngine(Renderer engine) {
        if (this.engine != engine) {
            this.engine = engine;

            for (GameComponent component : components)
                component.AddToEngine(engine);

            for (RenderObject child : children)
                child.SetEngine(engine);
        }
    }
}
