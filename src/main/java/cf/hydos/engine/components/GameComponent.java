package cf.hydos.engine.components;

import cf.hydos.engine.core.Renderer;
import cf.hydos.engine.core.RenderObject;
import cf.hydos.engine.rendering.RenderingEngine;
import cf.hydos.engine.rendering.Shader;
import org.joml.Matrix4f;

public abstract class GameComponent {
    private RenderObject parent;

    public void Input(float delta) {
    }

    public void Update(float delta) {
    }

    public void Render(Shader shader, RenderingEngine renderingEngine) {
    }

    public void SetParent(RenderObject parent) {
        this.parent = parent;
    }

    public Matrix4f GetTransform() {
        return parent.getTransformation();
    }

    public void AddToEngine(Renderer engine) {
    }
}

