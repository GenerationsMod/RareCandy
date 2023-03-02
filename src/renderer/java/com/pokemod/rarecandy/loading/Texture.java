package com.pokemod.rarecandy.loading;

import com.pokemod.pokeutils.reader.TextureReference;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.file.Paths;

public class Texture {

    public final String name;
    public final int id;

    public Texture(TextureReference reference) {
        this.name = reference.name();
        this.id = GL11C.glGenTextures();

        GL20C.glActiveTexture(GL20C.GL_TEXTURE0);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, reference.data().getWidth(), reference.data().getHeight(), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, reference.data().getPixelsRGBA());

        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

    }

    public Texture(byte[] imageBytes, String name) {
        this.name = name;
        this.id = GL11C.glGenTextures();

        try (var stack = MemoryStack.stackPush()) {
            var fileBytes = CubeMapTexture.readResource(imageBytes);
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);
            var rgbaBytes = STBImage.stbi_load_from_memory(fileBytes, width, height, channels, 4);
            if (rgbaBytes == null) throw new RuntimeException("Failed to load image.");

            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width.get(0), height.get(0), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, rgbaBytes);

            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
        }
    }

    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }
}
