package com.pixelmongenerations.pixelmonassetutils.scene.material;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture {

    public final String name;
    public final int id;

    public Texture(ByteBuffer imageFileBytes, String name) {
        this.name = name;
        this.id = GL45C.glCreateTextures(GL11C.GL_TEXTURE_2D);

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer numComponents = BufferUtils.createIntBuffer(1);

        if (!STBImage.stbi_info_from_memory(imageFileBytes, w, h, numComponents)) {
            throw new RuntimeException("Failed to read image information: " + STBImage.stbi_failure_reason());
        }

        ByteBuffer buffer = STBImage.stbi_load_from_memory(imageFileBytes, w, h, numComponents, 4);
        if (buffer == null) {
            throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
        }

        GL45C.glTextureParameteri(this.id, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL45C.glTextureParameteri(this.id, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
        GL45C.glTextureParameteri(this.id, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL45C.glTextureParameteri(this.id, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

        GL45C.glTextureStorage2D(this.id, 1, GL11C.GL_RGBA8, w.get(0), h.get(0));
        GL45C.glTextureSubImage2D(this.id, 0, 0, 0, w.get(0), h.get(0), GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, buffer);
        GL45C.glGenerateTextureMipmap(this.id);
    }

    public void bind(int slot) {
        GL45C.glBindTextureUnit(slot, this.id);
    }
}
