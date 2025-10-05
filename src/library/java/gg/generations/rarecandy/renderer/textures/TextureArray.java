package gg.generations.rarecandy.renderer.textures;


import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL45C.*;

/**
 * Fixed-size texture array with explicit layer filling.
 * Views for each layer are created at construction.
 */
public final class TextureArray implements AutoCloseable {

    private final int textureId;
    private final int width;
    private final int height;
    private final ITexture.Type type;
    private final int layers;
    private final List<ITexture> layerViews;

    public TextureArray(int width, int height, int layers, boolean useViews) {
        this(width, height, ITexture.Type.RGBA_BYTE, layers, useViews);
    }

    public TextureArray(int width, int height, ITexture.Type type, int layers, boolean useViews) {
        this.width = width;
        this.height = height;
        this.type = type;
        this.layers = layers;

        // allocate array
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
        glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, type.internalFormat, width, height, layers);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        // create views for each layer

        if (useViews) {
            layerViews = new ArrayList<>(layers);
            for (int i = 0; i < layers; i++) {
                layerViews.add(createLayerView(i));
            }
        } else {
            layerViews = Collections.emptyList();
        }
    }

    /**
     * Copies the contents of an existing ITexture into a specific layer.
     * Dimensions must match exactly.
     */
    public void fillLayer(int layerIndex, ITexture src) {
        checkLayerIndex(layerIndex);
//        if (src.width() != width || src.height() != height) {
//            throw new IllegalArgumentException("Source texture dimensions must match texture array layer dimensions");
//        }

        glCopyImageSubData(
                src.getId(), GL_TEXTURE_2D, 0, 0, 0, 0,
                textureId, GL_TEXTURE_2D_ARRAY, 0, 0, 0, layerIndex,
                src.width(), src.height(), 1
        );
    }

    /**
     * Uploads raw RGBA8 pixel data into a specific layer.
     * Buffer size must exactly match width*height*4.
     */
    public void fillLayer(int layerIndex, ByteBuffer pixels) {
        checkLayerIndex(layerIndex);
        if (pixels.remaining() != width * height * 4) {
            throw new IllegalArgumentException("Pixel buffer size mismatch for layer " + layerIndex);
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId);
        glTexSubImage3D(
                GL_TEXTURE_2D_ARRAY,
                0,
                0, 0, layerIndex,
                width, height, 1,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                pixels
        );
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    /** Returns the ITexture view for a specific layer. */
    public ITexture getLayerTexture(int layerIndex) {
        checkLayerIndex(layerIndex);
        return layerViews.get(layerIndex);
    }

    public int getId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLayerCount() {
        return layers;
    }

    @Override
    public void close() throws IOException {
        for (ITexture view : layerViews) {
            view.close();
        }
        glDeleteTextures(textureId);
    }

    // ─────────── Internal ───────────

    private void checkLayerIndex(int layerIndex) {
        if (layerIndex < 0 || layerIndex >= layers) {
            throw new IllegalArgumentException("Invalid layer index: " + layerIndex);
        }
    }

    private ITexture createLayerView(int layerIndex) {
        int viewTex = glGenTextures();
        glTextureView(
                viewTex,
                GL_TEXTURE_2D,
                textureId,
                GL_RGBA8,
                0, 1,
                layerIndex, 1
        );

        glTextureParameteri(viewTex, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(viewTex, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTextureParameteri(viewTex, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTextureParameteri(viewTex, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        final int idx = layerIndex;
        return new ITexture() {
            @Override
            public void bind(int slot) {
                ITexture.super.bind(slot);
            }

            @Override
            public int width() { return width; }

            @Override
            public int height() { return height; }

            @Override
            public int getId() { return viewTex; }

            @Override
            public Type getType() { return Type.RGBA_BYTE; }

            @Override
            public void close() throws IOException {
                glDeleteTextures(viewTex);
            }

            @Override
            public String toString() {
                return "TextureArrayLayer[" + idx + "]";
            }
        };
    }

    public ITexture.Type getType() {
        return type;
    }
}
