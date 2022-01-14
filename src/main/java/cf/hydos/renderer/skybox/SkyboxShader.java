package cf.hydos.renderer.skybox;

import cf.hydos.renderer.shaders.ShaderProgram;
import cf.hydos.renderer.shaders.UniformMatrix;
import cf.hydos.renderer.utils.MyFile;

public class SkyboxShader extends ShaderProgram {

    private static final MyFile VERTEX_SHADER = new MyFile("shaders/skybox", "skyboxVertex.glsl");
    private static final MyFile FRAGMENT_SHADER = new MyFile("shaders/skybox", "skyboxFragment.glsl");

    protected final UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");

    public SkyboxShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position");
        super.storeAllUniformLocations(projectionViewMatrix);
    }
}
