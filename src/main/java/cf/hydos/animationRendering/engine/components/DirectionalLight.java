package cf.hydos.animationRendering.engine.components;

import cf.hydos.animationRendering.engine.core.Vector3f;
import cf.hydos.animationRendering.engine.rendering.Shader;

public class DirectionalLight extends BaseLight {
    public DirectionalLight(Vector3f color, float intensity) {
        super(color, intensity);

        SetShader(new Shader("forward-directional"));
    }

    public Vector3f GetDirection() {
        return GetTransform().GetTransformedRot().GetForward();
    }
}
