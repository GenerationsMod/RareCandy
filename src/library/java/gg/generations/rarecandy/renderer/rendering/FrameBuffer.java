package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.loading.ITexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;

import java.nio.ByteBuffer;

public class FrameBuffer implements ITexture {
    private final int framebufferId;
    private final int textureId;
    private final int rbo;
    private final int width;
    private final int height;

    public FrameBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        framebufferId = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureId, 0);

        rbo = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete!");
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void bindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
    }

    public void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void bind(int slot) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    @Override
    public void close() {
        GL30.glDeleteFramebuffers(framebufferId);
        GL11.glDeleteTextures(textureId);
        GL30.glDeleteRenderbuffers(rbo);
    }

    public void captureScreenshot(String filePath, boolean isPortrait) {
        var scale = isPortrait ? 2 : 4;
        var newWidth = (width / scale);
        var newHeight = (height / scale);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        ByteBuffer resizedBuffer = BufferUtils.createByteBuffer(newWidth * newHeight * 4);
        STBImageResize.stbir_resize_uint8(buffer, width, height, 0, resizedBuffer, newWidth, newHeight, 0, 4);
        STBImageWrite.stbi_flip_vertically_on_write(true);
        STBImageWrite.stbi_write_png(filePath, newWidth, newHeight, 4, resizedBuffer, newWidth * 4);


        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
