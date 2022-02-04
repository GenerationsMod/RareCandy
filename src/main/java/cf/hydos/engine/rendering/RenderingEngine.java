package cf.hydos.engine.rendering;

import cf.hydos.engine.Window;
import cf.hydos.engine.core.RenderObject;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

public class RenderingEngine {

    public final Matrix4f projViewMatrix;

    public RenderingEngine(Window window) {
        this.projViewMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f).lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        GL11C.glClearColor(63 / 255f, 191 / 255f, 217 / 255f, 1.0f);

        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
    }

    public void render(RenderObject object) {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

        object.render(this);

        GL11C.glEnable(GL11C.GL_BLEND);
        GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
        GL11C.glDepthMask(false);
        GL11C.glDepthFunc(GL11C.GL_EQUAL);
        GL11C.glDepthFunc(GL11C.GL_LESS);
        GL11C.glDepthMask(true);
        GL11C.glDisable(GL11C.GL_BLEND);
    }
}
