package cf.hydos.animationRendering.engine.core;

import cf.hydos.animationRendering.engine.components.GameComponent;
import cf.hydos.animationRendering.engine.rendering.RenderingEngine;
import cf.hydos.animationRendering.engine.rendering.Shader;

import java.util.ArrayList;

public class GameObject {
    private final ArrayList<GameObject> m_children;
    private final ArrayList<GameComponent> m_components;
    private final Transform m_transform;
    private CoreEngine m_engine;

    public GameObject() {
        m_children = new ArrayList<GameObject>();
        m_components = new ArrayList<GameComponent>();
        m_transform = new Transform();
        m_engine = null;
    }

    public GameObject AddChild(GameObject child) {
        m_children.add(child);
        child.SetEngine(m_engine);
        child.GetTransform().SetParent(m_transform);

        return this;
    }

    public GameObject AddComponent(GameComponent component) {
        m_components.add(component);
        component.SetParent(this);

        return this;
    }

    public void InputAll(float delta) {
        Input(delta);

        for (GameObject child : m_children)
            child.InputAll(delta);
    }

    public void UpdateAll(float delta) {
        Update(delta);

        for (GameObject child : m_children)
            child.UpdateAll(delta);
    }

    public void RenderAll(Shader shader, RenderingEngine renderingEngine) {
        Render(shader, renderingEngine);

        for (GameObject child : m_children)
            child.RenderAll(shader, renderingEngine);
    }

    public void Input(float delta) {
        m_transform.Update();

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

    public ArrayList<GameObject> GetAllAttached() {
        ArrayList<GameObject> result = new ArrayList<GameObject>();

        for (GameObject child : m_children)
            result.addAll(child.GetAllAttached());

        result.add(this);
        return result;
    }

    public Transform GetTransform() {
        return m_transform;
    }

    public void SetEngine(CoreEngine engine) {
        if (this.m_engine != engine) {
            this.m_engine = engine;

            for (GameComponent component : m_components)
                component.AddToEngine(engine);

            for (GameObject child : m_children)
                child.SetEngine(engine);
        }
    }
}
