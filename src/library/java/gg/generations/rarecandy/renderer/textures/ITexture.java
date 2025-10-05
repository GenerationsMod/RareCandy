package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;

import java.io.Closeable;

public interface ITexture extends Closeable {
    default void bind(int slot) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getId());
    }

    int width();
    int height();
    int getId();

    Type getType();

    default ComputeAccess access() {
        return ComputeAccess.READ_ONLY;
    }

    enum Type {
        RGBA_BYTE(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE),
        RGB_BYTE(GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE);

        public final int internalFormat;
        public final int format;
        public final int type;

        Type(int internalFormat, int format, int type) {
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
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
