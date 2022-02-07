package com.pixelmongenerations.legacy.inception.components;

import com.pixelmongenerations.legacy.inception.core.RenderObject;
import org.joml.Matrix4f;

/**
 * @deprecated Planned to convert the GameComponent system into RenderObject's
 */
@Deprecated
public abstract class GameComponent {
    private RenderObject parent;

    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
    }

    public void SetParent(RenderObject parent) {
        this.parent = parent;
    }

    public Matrix4f GetTransform() {
        return parent.getTransformation();
    }

    public abstract void update();
}

