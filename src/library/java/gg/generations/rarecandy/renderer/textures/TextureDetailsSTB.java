package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

record TextureDetailsSTB(ByteBuffer buffer, Texture.Type type, int width, int height) implements TextureDetails {
    @Override
    public void close() {}

    public int init() {
        var id = GL11.glGenTextures();
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, type.internalFormat, width, height, 0, type.format, type.type, buffer);

        MemoryUtil.memFree(buffer);

        return id;
    }
}
