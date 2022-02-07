package com.pixelmongenerations.pixelmonassetutils.scene.material;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;

public class Texture {

    public final String name;
    public final int id;

    public Texture(ByteBuffer imageFileBytes, String name) {
        this.name = name;
        this.id = GL11C.glGenTextures();

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer numComponents = BufferUtils.createIntBuffer(1);

        if (!stbi_info_from_memory(imageFileBytes, w, h, numComponents)) {
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
        }

        ByteBuffer buffer = stbi_load_from_memory(imageFileBytes, w, h, numComponents, 4);
        if (buffer == null) {
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
        }

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);

        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, w.get(0), h.get(0), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, buffer);
    }

    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }
}
