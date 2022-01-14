package cf.hydos.renderer.skybox;

import cf.hydos.renderer.main.Camera;
import cf.hydos.renderer.openglObjects.VertexAttributesObject;
import cf.hydos.renderer.utils.OpenGlUtils;
import org.lwjgl.opengl.GL11;

public class SkyboxRenderer {

    private static final float SIZE = 200;

    private final SkyboxShader shader;
    private final VertexAttributesObject box;

    public SkyboxRenderer() {
        this.shader = new SkyboxShader();
        this.box = CubeGenerator.generateCube(SIZE);
    }

    /**
     * Renders the animationrenderer.skybox.
     */
    public void render() {
        prepare();
        box.bind(0);
        GL11.glDrawElements(GL11.GL_TRIANGLES, box.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
        box.unbind(0);
        shader.stop();
    }

    /**
     * Delete the shader when the game closes.
     */
    public void cleanUp() {
        shader.cleanUp();
    }

    /**
     * Starts the shader, loads the projection-view matrix to the uniform
     * variable, and sets some OpenGL state which should be mostly
     * self-explanatory.
     */
    private void prepare() {
        shader.start();
        shader.projectionViewMatrix.loadMatrix(Camera.createProjectionMatrix());
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.disableBlending();
        OpenGlUtils.cullBackFaces(true);
        OpenGlUtils.antialias(false);
    }
}
