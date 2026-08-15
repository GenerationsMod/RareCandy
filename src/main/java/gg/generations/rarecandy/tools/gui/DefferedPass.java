package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.renderer.rendering.StateManager;
import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;

public class DefferedPass {
    private static final int ALBEDO_ATTACHMENT = 0;
    private static final int NORMAL_ATTACHMENT = 1;
    private static final int EMISSION_ATTCHMENT = 2;
    private static final int SELECTION_ATTCHMENT = 3;
    private static final int DEPTH_ATTCHMENT = 4;

    private final FrameBuffer framebuffer;
    private final StateManager manager;
//    private final ScreenSpaceGridRenderer grid;

    public DefferedPass(FrameBuffer framebuffer, StateManager manager, ScreenSpaceGridRenderer grid) {
        this.framebuffer = framebuffer;
        this.manager = manager;
        this.grid = grid;
    }

    public void start(Vector3f clear) {
        framebuffer.bindAndSetViewport();
        framebuffer.setDrawAll();
        framebuffer.clearColor(ALBEDO_ATTACHMENT, clear.x, clear.y, clear.z, clear.w);
        framebuffer.clearColor(NORMAL_ATTACHMENT, 0.5f, 0.5f, 0.5f, 0.0f);
        framebuffer.clearColor(EMISSION_ATTCHMENT, 0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clearColor(SELECTION_ATTCHMENT, 0f, 0f, 0f, 0f);
        framebuffer.clearDepth(1.0f);
    }

    public void render(RareCandy renderer, PokeUtilsGui.Settings settings, int screenWidth, int screenHeight, Vector4f clearColor) {




        GuiPipelines.DEFERRED.useProgram();
        GuiPipelines.DEFERRED.bindGlobal();
        renderer.render(GuiPipelines.DEFERRED, manager);

//        framebuffer.setDrawBuffers(DEFERRED_COLOR);
//        grid.render(framebuffer.width(), framebuffer.height(), settings, manager);

        framebuffer.unbind();
        GL11C.glViewport(0, 0, screenWidth, screenHeight);

        manager.reset();
        GuiPipelines.DEFERRED_COMPOSITE.useProgram();
        GuiPipelines.DEFERRED_COMPOSITE.bindGlobal();
        GL11C.glDrawArrays(GL11C.GL_TRIANGLES, 0, 3);
    }
}