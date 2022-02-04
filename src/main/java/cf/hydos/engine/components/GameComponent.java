package cf.hydos.engine.components;

import cf.hydos.engine.core.RenderObject;
import org.joml.Matrix4f;

public abstract class GameComponent {
    private RenderObject parent;

    public void Render(Matrix4f projViewMatrix) {
    }

    public void SetParent(RenderObject parent) {
        this.parent = parent;
    }

    public Matrix4f GetTransform() {
        return parent.getTransformation();
    }

}

