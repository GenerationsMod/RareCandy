package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;

import java.io.Closeable;

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
        return GL11.GL_TEXTURE_2D;
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

    enum Type {
        RGBA8(GL30.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false),
        RGB8(GL30.GL_RGB8, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false),

        R8(GL30.GL_R8, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false),
        RG8(GL30.GL_RG8, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE, true, false, false, false, false),

        R16F(GL30.GL_R16F, GL11.GL_RED, GL11.GL_FLOAT, true, false, false, false, false),
        R32F(GL30.GL_R32F, GL11.GL_RED, GL11.GL_FLOAT, true, false, false, false, false),
        RG16F(GL30.GL_RG16F, GL30.GL_RG, GL11.GL_FLOAT, true, false, false, false, false),
        RG32F(GL30.GL_RG32F, GL30.GL_RG, GL11.GL_FLOAT, true, false, false, false, false),
        RGB16F(GL30.GL_RGB16F, GL11.GL_RGB, GL11.GL_FLOAT, true, false, false, false, false),
        RGBA16F(GL30.GL_RGBA16F, GL11.GL_RGBA, GL11.GL_FLOAT, true, false, false, false, false),
        RGBA32F(GL30.GL_RGBA32F, GL11.GL_RGBA, GL11.GL_FLOAT, true, false, false, false, false),

        R32I(GL30.GL_R32I, GL30.GL_RED_INTEGER, GL11.GL_INT, true, true, false, false, false),
        R32UI(GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, true, true, false, false, false),
        RGBA8UI(GL30.GL_RGBA8UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_BYTE, true, true, false, false, false),
        RGBA16UI(GL30.GL_RGBA16UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_SHORT, true, true, false, false, false),
        RGBA32UI(GL30.GL_RGBA32UI, GL30.GL_RGBA_INTEGER, GL11.GL_UNSIGNED_INT, true, true, false, false, false),

        DEPTH16(GL30.GL_DEPTH_COMPONENT16, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, false, false, true, false, false),
        DEPTH24(GL30.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, false, false, true, false, false),
        DEPTH32F(GL30.GL_DEPTH_COMPONENT32F, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, false, false, true, false, false),

        STENCIL8(GL30.GL_STENCIL_INDEX8, GL30.GL_STENCIL_INDEX, GL11.GL_UNSIGNED_BYTE, false, false, false, true, false),

        DEPTH24_STENCIL8(GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, false, false, true, true, true),
        DEPTH32F_STENCIL8(GL30.GL_DEPTH32F_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, false, false, true, true, true);

        public final int internalFormat;
        public final int format;
        public final int type;
        public final boolean colorRenderable;
        public final boolean integer;
        public final boolean depthLike;
        public final boolean stencilLike;
        public final boolean depthStencil;

        Type(int internalFormat,
             int format,
             int type,
             boolean colorRenderable,
             boolean integer,
             boolean depthLike,
             boolean stencilLike,
             boolean depthStencil) {
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
            this.colorRenderable = colorRenderable;
            this.integer = integer;
            this.depthLike = depthLike;
            this.stencilLike = stencilLike;
            this.depthStencil = depthStencil;
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