package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import org.joml.Vector4f;

public class DefferedPass {
    private static final int ALBEDO_ATTACHMENT = 0;
    private static final int NORMAL_ATTACHMENT = 1;
    private static final int EMISSION_ATTACHMENT = 2;
    private static final int SELECTION_ATTACHMENT = 3;

    public static void start(FrameBuffer framebuffer, Vector4f clear) {
        framebuffer.bindAndSetViewport();
        framebuffer.setDrawAll();
        framebuffer.clearColor(ALBEDO_ATTACHMENT, clear.x, clear.y, clear.z, clear.w);
        framebuffer.clearColor(NORMAL_ATTACHMENT, 0.5f, 0.5f, 0.5f, 1.0f);
        framebuffer.clearColor(EMISSION_ATTACHMENT, 0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clearColor(SELECTION_ATTACHMENT, 0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clearDepth(1.0f);
    }
}