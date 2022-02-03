package cf.hydos.engine.rendering;

import cf.hydos.engine.Window;
import cf.hydos.engine.core.RenderObject;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL43C;

import static org.lwjgl.opengl.GL11.*;

public class RenderingEngine {

    private final Shader forwardAmbient;
    public final Matrix4f projViewMatrix;

    public RenderingEngine(Window window) {
        this.projViewMatrix = new Matrix4f().perspective((float) Math.toRadians(45), (float) window.width / window.height, 0.1f, 1000.0f).lookAt(2.0f, 0.1f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        forwardAmbient = new Shader("animated");

        glClearColor(63 / 255f, 191 / 255f, 217 / 255f, 1.0f);

        glFrontFace(GL_CW);
        glCullFace(GL_FRONT);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

    public void Render(RenderObject object) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        object.onRender(this);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glDepthMask(false);
        glDepthFunc(GL_EQUAL);

        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }
}
