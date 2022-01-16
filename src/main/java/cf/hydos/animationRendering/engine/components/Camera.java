package cf.hydos.animationRendering.engine.components;

import cf.hydos.animationRendering.engine.core.CoreEngine;
import cf.hydos.animationRendering.engine.core.Matrix4f;
import cf.hydos.animationRendering.engine.core.Vector3f;

public class Camera extends GameComponent {
    private final Matrix4f m_projection;

    public Camera(Matrix4f projection) {
        this.m_projection = projection;
    }

    public Matrix4f GetViewProjection() {
        Matrix4f cameraRotation = GetTransform().GetTransformedRot().Conjugate().ToRotationMatrix();
        Vector3f cameraPos = GetTransform().GetTransformedPos().Mul(-1);

        Matrix4f cameraTranslation = new Matrix4f().InitTranslation(cameraPos.GetX(), cameraPos.GetY(), cameraPos.GetZ());

        return m_projection.Mul(cameraRotation.Mul(cameraTranslation));
    }

    @Override
    public void AddToEngine(CoreEngine engine) {
        engine.GetRenderingEngine().AddCamera(this);
    }
}
