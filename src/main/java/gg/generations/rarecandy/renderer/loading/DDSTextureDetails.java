package gg.generations.rarecandy.renderer.loading;

import io.github.mudbill.dds.DDSFile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import java.io.IOException;

public record DDSTextureDetails(DDSFile file) implements TextureDetails {
    @Override
    public int init() {
        int textureID = GL11.glGenTextures();       // Generate a texture ID.
        GL13.glActiveTexture(GL13.GL_TEXTURE0);     // Depends on your implementation
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        for (int level = 0; level < file.getMipMapCount(); level++)
            GL13.glCompressedTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    level,
                    file.getFormat(),
                    file.getWidth(level),
                    file.getHeight(level),
                    0,
                    file.getBuffer(level)
            );
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, file.getMipMapCount() - 1);
        return textureID;
    }

    @Override
    public int width() {
        return file.getWidth();
    }

    @Override
    public int height() {
        return file.getHeight();
    }

    @Override
    public void close() throws IOException {

    }
}
