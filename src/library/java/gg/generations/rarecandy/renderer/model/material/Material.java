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

    private final boolean disableDepth;

    private final long pointer;

    public Material(int[] images, MaterialValues values, boolean disableDepth, CullType cullType, BlendType blendType, int colorMethod, int effect) {
        this.images = images;
        this.disableDepth = disableDepth;
        this.cullType = cullType;
        this.blendType = blendType;
        this.values = values;
        this.colorMethod = colorMethod;
        this.effect = effect;

        this.pointer = MemoryUtil.nmemAlloc(208);
        updateUbo();
    }

    public void updateUbo() {
        values.put(pointer);
        MemoryUtil.memPutInt(pointer + 176, colorMethod);
        MemoryUtil.memPutInt(pointer + 180, effect);
        MemoryUtil.memPutInt(pointer + 184, values.getUseLight() ? 1 : 0);
        MemoryUtil.memPutInt(pointer + 188, images[0]);
        MemoryUtil.memPutInt(pointer + 192, images[1]);
        MemoryUtil.memPutInt(pointer + 196, images[2]);
        MemoryUtil.memPutInt(pointer + 200, images[3]);
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
        return disableDepth;
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
