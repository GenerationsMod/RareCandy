package cf.hydos.engine.core;

import cf.hydos.engine.components.GameComponent;
import cf.hydos.engine.rendering.RenderingEngine;
import cf.hydos.engine.rendering.Shader;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class RenderObject {
    private final ArrayList<RenderObject> m_children;
    private final ArrayList<GameComponent> m_components;
    private final Matrix4f m_transform;
    private Renderer m_engine;

    public RenderObject() {
        m_children = new ArrayList<RenderObject>();
        m_components = new ArrayList<GameComponent>();
        m_transform = new Matrix4f();
        m_engine = null;
    }

    public RenderObject AddChild(RenderObject child) {
        m_children.add(child);
        child.SetEngine(m_engine);
        child.getTransformation().mul(m_transform);

        return this;
    }

    public RenderObject addComponent(GameComponent component) {
        m_components.add(component);
        component.SetParent(this);

        return this;
    }

    public void InputAll(float delta) {
        Input(delta);

        for (RenderObject child : m_children)
            child.InputAll(delta);
    }

    public void UpdateAll(float delta) {
        Update(delta);

        for (RenderObject child : m_children)
            child.UpdateAll(delta);
    }

    public void RenderAll(Shader shader, RenderingEngine renderingEngine) {
        Render(shader, renderingEngine);

        for (RenderObject child : m_children)
            child.RenderAll(shader, renderingEngine);
    }

    public void Input(float delta) {
        for (GameComponent component : m_components)
            component.Input(delta);
    }

    public void Update(float delta) {
        for (GameComponent component : m_components)
            component.Update(delta);
    }

    public void Render(Shader shader, RenderingEngine renderingEngine) {
        for (GameComponent component : m_components)
            component.Render(shader, renderingEngine);
    }

    public ArrayList<RenderObject> GetAllAttached() {
        ArrayList<RenderObject> result = new ArrayList<RenderObject>();

        for (RenderObject child : m_children)
            result.addAll(child.GetAllAttached());

        result.add(this);
        return result;
    }

    public Matrix4f getTransformation() {
        return m_transform;
    }

    public void SetEngine(Renderer engine) {
        if (this.m_engine != engine) {
            this.m_engine = engine;

            for (GameComponent component : m_components)
                component.AddToEngine(engine);

            for (RenderObject child : m_children)
                child.SetEngine(engine);
        }
    }
}
