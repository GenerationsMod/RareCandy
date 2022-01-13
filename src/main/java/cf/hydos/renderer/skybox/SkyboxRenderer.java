package cf.hydos.renderer.skybox;

import cf.hydos.renderer.main.Camera;
import cf.hydos.renderer.openglObjects.Vao;
import cf.hydos.renderer.scene.ICamera;
import cf.hydos.renderer.utils.OpenGlUtils;
import org.lwjgl.opengl.GL11;

public class SkyboxRenderer {

    private static final float SIZE = 200;

    private final SkyboxShader shader;
    private final Vao box;

    public SkyboxRenderer() {
        this.shader = new SkyboxShader();
        this.box = CubeGenerator.generateCube(SIZE);
    }

    /**
     * Renders the com.thinmatrix.animationrenderer.skybox.
     *
     * @param camera - the com.thinmatrix.animationrenderer.scene's camera.
     */
    public void render(ICamera camera) {
        prepare(camera);
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
     *
     * @param camera - the com.thinmatrix.animationrenderer.scene's camera.
     */
    private void prepare(ICamera camera) {
        shader.start();
        shader.projectionViewMatrix.loadMatrix(Camera.createProjectionMatrix());
        OpenGlUtils.enableDepthTesting(true);
        OpenGlUtils.disableBlending();
        OpenGlUtils.cullBackFaces(true);
        OpenGlUtils.antialias(false);
    }
}
