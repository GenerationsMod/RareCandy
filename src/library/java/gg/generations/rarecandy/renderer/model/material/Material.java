package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.loading.Texture;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import org.lwjgl.opengl.GL40;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class Material implements Closeable {
    private final String materialName;
    private Map<String, CloseableSupplier<Texture>> images;
    private CullType cullType;
    private BlendType blendType;

    private String shader;

    public Material(String materialName, Map<String, CloseableSupplier<Texture>> images, CullType cullType, BlendType blendType, String shader) {
        this.materialName = materialName;
        this.images = images;
        this.cullType = cullType;
        this.blendType = blendType;
        this.shader = shader;
    }

    public Texture getDiffuseTexture() {
        return images.getOrDefault("diffuse", ImageSupplier.BLANK).get();
    }

    public Pipeline getPipeline() {
        return PipelineRegistry.get(shader);
    }

    public CullType cullType() {
        return cullType;
    }

    public BlendType blendType() {
        return blendType;
    }

    public Texture getTexture(String imageType) {
        return images.get(imageType).get();
    }

    public String getMaterialName() {
        return materialName;
    }

    @Override
    public void close() throws IOException {
        if(images != null) {
            for (var texture : images.values()) {
                texture.close();
            }
        }
    }

    public static class ImageSupplier implements CloseableSupplier<Texture> {
        public static final CloseableSupplier<Texture> BLANK = new CloseableSupplier<>() {
            @Override
            public void close() throws IOException {

            }

            @Override
            public Texture get() {
                return null;
            }
        };

        private final TextureReference textureReference;
        private Texture texture;

        public ImageSupplier(TextureReference textureReference) {
            this.textureReference = textureReference;
        }

        @Override
        public Texture get() {
            if (texture == null) this.texture = new Texture(textureReference);
            return texture;
        }

        @Override
        public void close() throws IOException {
            texture.close();
        }
    }

    public static interface CloseableSupplier<T> extends Supplier<T>, Closeable {

    }
}
