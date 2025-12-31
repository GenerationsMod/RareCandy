package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;

public class Material implements Closeable {
    private final int[] images;

    private final MaterialValues values;
    private final int colorMethod;
    private final int effect;

    private final CullType cullType;
    private final BlendType blendType;

    private final long pointer;

    public Material(int[] images, MaterialValues values, CullType cullType, BlendType blendType, int colorMethod, int effect) {
        this.images = images;
        this.cullType = cullType;
        this.blendType = blendType;
        this.values = values;
        this.colorMethod = colorMethod;
        this.effect = effect;

        this.pointer = MemoryUtil.nmemAlloc(188);
        updateUbo();
    }

    //TODO: Reworking memory upload
    public void updateUbo() {
        values.put(pointer);
        MemoryUtil.memPutInt(pointer + 176, colorMethod);
        MemoryUtil.memPutInt(pointer + 180, effect);
        MemoryUtil.memPutInt(pointer + 184, values.getUseParadox() ? 1: 0);
    }

    public int getColorMethod() {
        return colorMethod;
    }

    public int getEffect() {
        return effect;
    }

    public CullType cullType() {
        return cullType;
    }

    public BlendType blendType() {
        return blendType;
    }

    public int[] images() {
        return images;
    }

    @Override
    public void close() {
        MemoryUtil.nmemFree(pointer);
    }

    public boolean disableDepth() {
        return values().getDisableDepth();
    }

    public MaterialValues values() {
        return values;
    }

    public long getPointer() {
        return pointer;
    }

    public int bindMaterial() {
        MaterialUploader.INSTANCE.upload(this);

        return MaterialUploader.INSTANCE.id;
    }
}
