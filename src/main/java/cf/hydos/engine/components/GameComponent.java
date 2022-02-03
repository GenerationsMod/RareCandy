package cf.hydos.engine.components;

import cf.hydos.engine.core.LoopManager;
import cf.hydos.engine.core.RenderObject;
import cf.hydos.engine.rendering.Shader;
import org.joml.Matrix4f;

public abstract class GameComponent {
    private RenderObject parent;

    public void Input(float delta) {
    }

    public void Update(float delta) {
    }

    public void Render(Shader shader, Matrix4f projViewMatrix) {
    }

    public void SetParent(RenderObject parent) {
        this.parent = parent;
    }

    public Matrix4f GetTransform() {
        return parent.getTransformation();
    }

    public void AddToEngine(LoopManager engine) {
    }
}

