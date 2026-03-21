package gg.generations.rarecandy.renderer.textures.framebuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.IntBuffer;

public final class FramebufferDrawBuffers {
    private FramebufferDrawBuffers() {
    }

    public static void applyForColorAttachmentCount(int colorCount) {
        if (colorCount <= 0) {
            GL11.glDrawBuffer(GL11.GL_NONE);
            GL11.glReadBuffer(GL11.GL_NONE);
            return;
        }

        IntBuffer drawBuffers = BufferUtils.createIntBuffer(colorCount);
        for (int i = 0; i < colorCount; i++) {
            drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0 + i);
        }
        drawBuffers.flip();
        GL20.glDrawBuffers(drawBuffers);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
    }
}
