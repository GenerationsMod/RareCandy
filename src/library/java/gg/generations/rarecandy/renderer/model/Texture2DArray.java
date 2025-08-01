package gg.generations.rarecandy.renderer.model;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL42C.*;

public class Texture2DArray {

    private final int textureId;
    private final int width;
    private final int height;
    private final int layers;
    private final int format;
    private final int internalFormat;
    private final int type;

    public Texture2DArray(int width, int height, int layers, int internalFormat, int format, int type) {
        this.textureId = glGenTextures();
        this.width = width;
        this.height = height;
        this.layers = layers;
        this.format = format;
        this.internalFormat = internalFormat;
        this.type = type;

        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
        glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, internalFormat, width, height, layers);

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    public void uploadLayer(int layer, ByteBuffer data) {
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
        glTexSubImage3D(
                GL_TEXTURE_2D_ARRAY,
                0,
                0, 0, layer,
                width, height, 1,
                format,
                type,
                data
        );
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
    }

    public int getId() {
        return textureId;
    }

    public void delete() {
        glDeleteTextures(textureId);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLayers() {
        return layers;
    }
}
