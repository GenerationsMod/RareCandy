package cf.hydos.animationRendering.engine.components;

import cf.hydos.animationRendering.engine.core.CoreEngine;
import org.joml.Matrix4f;

public class Camera extends GameComponent {
    private final Matrix4f projectionView;

    public Camera(Matrix4f projection) {
        this.projectionView = projection.lookAt(2.0f, 0.1f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public Matrix4f getProjectionView() {
        return new Matrix4f(projectionView);
    }

    @Override
    public void AddToEngine(CoreEngine engine) {
        engine.GetRenderingEngine().AddCamera(this);
    }
}
