package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class Material implements Closeable {
    private final String materialName;
    private Map<String, String> images;

    private Map<String, Object> values;

    private CullType cullType;
    private BlendType blendType;

    private String shader;

    public Material(String materialName, Map<String, String> images, Map<String, Object> values, CullType cullType, BlendType blendType, String shader) {
        this.materialName = materialName;
        this.images = images;
        this.cullType = cullType;
        this.blendType = blendType;
        this.shader = shader;
        this.values = values;
    }

    public ITexture getDiffuseTexture() {
        return getTexture("diffuse");
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

    public ITexture getTexture(String imageType) {
        return ITextureLoader.instance().getTexture(images.get(imageType));
    }

    public Object getValue(String valueType) {
        return values.get(valueType);
    }

    public String getMaterialName() {
        return materialName;
    }

    @Override
    public String toString() {
        return materialName;
    }

    @Override
    public void close() throws IOException {
        if (images != null) {
            for (var texture : images.values()) {
                if (texture.equals(".")) ITextureLoader.instance().remove(texture);
            }
        }
    }

    public boolean getBoolean(String value) {
        return getValue(value) instanceof Boolean bool ? bool : false;
    }
}
