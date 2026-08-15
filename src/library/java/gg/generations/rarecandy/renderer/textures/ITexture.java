package gg.generations.rarecandy.renderer.textures;

import org.apache.commons.lang3.function.TriConsumer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public interface ITexture extends Closeable {
    default void bind(int slot) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(target(), id());
    }

    int width();
    int height();
    int id();
    Type type();

    default ComputeAccess access() {
        return ComputeAccess.READ_ONLY;
    }

    default int target() {
        return GL_TEXTURE_2D;
    }

    default long getSamplerHandle(SamplerDesc sampler) {
        throw new UnsupportedOperationException("Bindless sampler handle not implemented for " + getClass().getName());
    }

    default long getImageHandle(int level, boolean layered, ITexture.ComputeAccess access) {
        throw new UnsupportedOperationException("Bindless image handle not implemented for " + getClass().getName());
    }

    @Override
    default void close() throws java.io.IOException {
    }

    default void printToTexture(String name) {
        var type = type();
        int bytesPerComponent = switch (type.type) {
            case GL11.GL_BYTE, GL11.GL_UNSIGNED_BYTE -> 1;
            case GL11.GL_SHORT, GL11.GL_UNSIGNED_SHORT -> 2;
            default -> 4;
        };

        // stbi_write_png only emits 8-bit samples; anything else would be written as garbage.
        if (bytesPerComponent != 1) {
            throw new IllegalStateException("printToTexture only supports 8-bit types, got " + type);
        }

        int channels = type.channels;
        int stride = width() * channels * bytesPerComponent;

        ByteBuffer buffer = MemoryUtil.memAlloc(stride * height());

        try {
            bind(0);

            // Default pack alignment is 4, which pads rows for widths that aren't a multiple
            // of 4. Our stride assumes tightly packed rows, so drop the padding.
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glGetTexImage(GL_TEXTURE_2D, 0, type.format, type.type, buffer);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);

            STBImageWrite.stbi_write_png(
                    name + ".png",
                    width(),
                    height(),
                    channels, buffer,
                    stride
            );
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    enum Type {
        RGBA8(GL30.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false, 4),
        RGB8(GL30.GL_RGB8, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false, 3),

        R8(GL30.GL_R8, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false, 1),
        RG8(GL30.GL_RG8, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false, 2),

        R16F(GL30.GL_R16F, GL11.GL_RED, GL11.GL_FLOAT, true, false, false, false, false, 1),
        R32F(GL30.GL_R32F, GL11.GL_RED, GL11.GL_FLOAT, true, false, false, false, false, 2),
        RG16F(GL30.GL_RG16F, GL30.GL_RG, GL11.GL_FLOAT, true, false, false, false, false, 2),
        RG32F(GL30.GL_RG32F, GL30.GL_RG, GL11.GL_FLOAT, true, false, false, false, false, 2),
        RGB16F(GL30.GL_RGB16F, GL11.GL_RGB, GL11.GL_FLOAT, true, false, false, false, false, 3),
        RGBA16F(GL30.GL_RGBA16F, GL11.GL_RGBA, GL11.GL_FLOAT, true, false, false, false, false, 4),
        RGBA32F(GL30.GL_RGBA32F, GL11.GL_RGBA, GL11.GL_FLOAT, true, false, false, false, false, 4),

        R32I(GL30.GL_R32I, GL30.GL_RED_INTEGER, GL11.GL_INT, true, true, false, false, false, 1),
        R32UI(GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, true, true, false, false, false, 1),
        RGBA8UI(GL30.GL_RGBA8UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_BYTE, true, true, false, false, false, 4),
        RGBA16UI(GL30.GL_RGBA16UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_SHORT, true, true, false, false, false, 4),
        RGBA32UI(GL30.GL_RGBA32UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_INT, true, true, false, false, false, 4),

        DEPTH16(GL30.GL_DEPTH_COMPONENT16, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, false, false, true, false, false, 1),
        DEPTH24(GL30.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, false, false, true, false, false, 1),
        DEPTH32F(GL30.GL_DEPTH_COMPONENT32F, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, false, false, true, false, false, 1),

        STENCIL8(GL30.GL_STENCIL_INDEX8, GL30.GL_STENCIL_INDEX, GL11.GL_UNSIGNED_BYTE, false, false, false, true, false, 1),

        DEPTH24_STENCIL8(GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, false, false, true, true, true, 2),
        DEPTH32F_STENCIL8(GL30.GL_DEPTH32F_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, false, false, true, true, true, 2);

        public final int internalFormat;
        public final int format;
        public final int type;
        public final boolean colorRenderable;
        public final boolean integer;
        public final boolean depthLike;
        public final boolean stencilLike;
        public final boolean depthStencil;
        private final int channels;

        Type(int internalFormat,
             int format,
             int type,
             boolean colorRenderable,
             boolean integer,
             boolean depthLike,
             boolean stencilLike,
             boolean depthStencil,
             int channels) {
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
            this.colorRenderable = colorRenderable;
            this.integer = integer;
            this.depthLike = depthLike;
            this.stencilLike = stencilLike;
            this.depthStencil = depthStencil;
            this.channels = channels;
        }

        public boolean isColorRenderable() {
            return colorRenderable;
        }

        public boolean isInteger() {
            return integer;
        }

        public boolean isDepthLike() {
            return depthLike;
        }

        public boolean isStencilLike() {
            return stencilLike;
        }

        public boolean isDepthStencil() {
            return depthStencil;
        }
    }

    enum ComputeAccess {
        READ_ONLY(GL42.GL_READ_ONLY),
        WRITE_ONLY(GL42.GL_WRITE_ONLY),
        READ_WRITE(GL42.GL_READ_WRITE);

        private final int value;

        ComputeAccess(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}