package gg.generations.rarecandy.renderer.textures.framebuffer;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public final class FramebufferValidator {
    private FramebufferValidator() {
    }

    public static void assertComplete(int framebufferId) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer incomplete: " + describe(status) + " (0x" + Integer.toHexString(status) + ")");
        }
    }

    public static String describe(int status) {
        return switch (status) {
            case GL30.GL_FRAMEBUFFER_COMPLETE -> "GL_FRAMEBUFFER_COMPLETE";
            case GL30.GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            case GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
            default -> "UNKNOWN_FRAMEBUFFER_STATUS";
        };
    }
}
