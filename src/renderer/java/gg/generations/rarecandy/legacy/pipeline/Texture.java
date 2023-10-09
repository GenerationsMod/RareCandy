package gg.generations.rarecandy.legacy.pipeline;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.io.Closeable;
import java.nio.ByteBuffer;

public class Texture implements Closeable {

    public final int id;

    public Texture(ByteBuffer rgbaBytes, int width, int height) {
        this.id = GL11C.glGenTextures();

        GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, rgbaBytes);

        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

    }

    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
    }

    @Override
    public void close() {
        GL11C.glDeleteTextures(id);
    }
}
