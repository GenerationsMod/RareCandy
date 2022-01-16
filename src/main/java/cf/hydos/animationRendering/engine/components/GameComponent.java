package cf.hydos.animationRendering.engine.components;

import cf.hydos.animationRendering.engine.core.CoreEngine;
import cf.hydos.animationRendering.engine.core.GameObject;
import cf.hydos.animationRendering.engine.core.Transform;
import cf.hydos.animationRendering.engine.rendering.RenderingEngine;
import cf.hydos.animationRendering.engine.rendering.Shader;

public abstract class GameComponent {
    private GameObject m_parent;

    public void Input(float delta) {
    }

    public void Update(float delta) {
    }

    public void Render(Shader shader, RenderingEngine renderingEngine) {
    }

    public void SetParent(GameObject parent) {
        this.m_parent = parent;
    }

    public Transform GetTransform() {
        return m_parent.GetTransform();
    }

    public void AddToEngine(CoreEngine engine) {
    }
}

