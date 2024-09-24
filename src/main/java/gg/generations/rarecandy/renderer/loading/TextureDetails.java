package gg.generations.rarecandy.renderer.loading;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public interface TextureDetails extends AutoCloseable {
    int init();

    int width();

    int height();
}

record TextureDetailsSTB(ByteBuffer buffer, Texture.Type type, int width, int height) implements TextureDetails {
    @Override
    public void close() {
        MemoryUtil.memFree(buffer());
    }

    public int init() {
        var id = GL11.glGenTextures();
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, type.internalFormat, width, height, 0, type.format, type.type, buffer);

        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

        MemoryUtil.memFree(buffer);

        return id;
    }
}
