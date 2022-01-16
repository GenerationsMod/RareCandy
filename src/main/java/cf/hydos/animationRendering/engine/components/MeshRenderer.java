package cf.hydos.animationRendering.engine.components;

import cf.hydos.animationRendering.engine.rendering.Material;
import cf.hydos.animationRendering.engine.rendering.Mesh;
import cf.hydos.animationRendering.engine.rendering.RenderingEngine;
import cf.hydos.animationRendering.engine.rendering.Shader;

public class MeshRenderer extends GameComponent {
    private final Mesh m_mesh;
    private final Material m_material;

    public MeshRenderer(Mesh mesh, Material material) {
        this.m_mesh = mesh;
        this.m_material = material;
    }

    @Override
    public void Render(Shader shader, RenderingEngine renderingEngine) {
        shader.Bind();
        shader.UpdateUniforms(GetTransform(), m_material, renderingEngine);
        m_mesh.Draw();
    }
}
