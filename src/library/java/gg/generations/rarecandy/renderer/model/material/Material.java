package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class Material extends UniformBlockUploader implements Closeable {
    private final String materialName;
    private final MaterialImages images;

    private final MaterialValues values;
    private final int colorMethod;
    private final int effect;

    private final CullType cullType;
    private final BlendType blendType;

    private String shader;
    private final boolean disableDepth;

    public Material(int index, String materialName, MaterialImages images, MaterialValues values, boolean disableDepth, CullType cullType, BlendType blendType, String shader, int colorMethod, int effect) {
        super(192, index);

        this.materialName = materialName;
        this.images = images;
        this.disableDepth = disableDepth;
        this.cullType = cullType;
        this.blendType = blendType;
        this.shader = shader;
        this.values = values;
        this.colorMethod = colorMethod;
        this.effect = effect;
    }

    public int getColorMethod() {
        return colorMethod;
    }

    public int getEffect() {
        return effect;
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
    public void close() {
        super.close();
        if(images != null) {
            if(images.getDiffuse().contains(".")) ITextureLoader.instance().remove(images.getDiffuse());
            if(images.getEmission().contains(".")) ITextureLoader.instance().remove(images.getDiffuse());
            if(images.getLayer().contains(".")) ITextureLoader.instance().remove(images.getLayer());
            if(images.getMask().contains(".")) ITextureLoader.instance().remove(images.getMask());
        }
    }

    public void update() {
        initalize();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(192);
            values().toAddress(address);
            MemoryUtil.memPutInt(address + 184, effect);
            MemoryUtil.memPutInt(address + 188, colorMethod);
            upload(0, 192, address);
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
