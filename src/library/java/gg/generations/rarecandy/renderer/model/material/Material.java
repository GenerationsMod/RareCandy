package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.loading.ITexture;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class Material implements Closeable {
    private final String materialName;
    private final MaterialImages images;

    private final MaterialValues values;

    private final CullType cullType;
    private final BlendType blendType;

    private String shader;
    private final boolean disableDepth;

    public Material(String materialName, MaterialImages images, MaterialValues values, boolean disableDepth, CullType cullType, BlendType blendType, String shader) {
        this.materialName = materialName;
        this.images = images;
        this.disableDepth = disableDepth;
        this.cullType = cullType;
        this.blendType = blendType;
        this.shader = shader;
        this.values = values;
    }

    public String getPipeline() {
        return shader;
    }

    public CullType cullType() {
        return cullType;
    }

    public BlendType blendType() {
        return blendType;
    }

    public MaterialImages images() {
        return images;
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
        if(images != null) {
            if(images.getDiffuse().contains(".")) ITextureLoader.instance().remove(images.getDiffuse());
            if(images.getEmission().contains(".")) ITextureLoader.instance().remove(images.getDiffuse());
            if(images.getLayer().contains(".")) ITextureLoader.instance().remove(images.getLayer());
            if(images.getMask().contains(".")) ITextureLoader.instance().remove(images.getMask());
        }
    }

    public int maxTextureSize() {
        return images.stream().map(ITextureLoader.instance()::getTexture).mapToInt(ITexture::width).max().getAsInt();
    }

    public void setShader(String solid) {
        shader = solid;
    }

    public boolean disableDepth() {
        return disableDepth;
    }

    public MaterialValues values() {
        return values;
    }
}
