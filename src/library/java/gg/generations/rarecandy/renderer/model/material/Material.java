package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;

public class Material implements Closeable {
    private final long[] handles;

    private final MaterialValues values;
    private final int colorMethod;
    private final int[] effect;

    private final CullType cullType;
    private final BlendType blendType;

    private final boolean disableDepth;

    private final long pointer;

    public Material(long[] images, MaterialValues values, boolean disableDepth, CullType cullType, BlendType blendType, int colorMethod, int effect[]) {
        this.handles = images;
        this.disableDepth = disableDepth;
        this.cullType = cullType;
        this.blendType = blendType;
        this.values = values;
        this.colorMethod = colorMethod;
        this.effect = effect;

        this.pointer = MemoryUtil.nmemAlloc(220);
        updateUbo();
    }

    //TODO: Reworking memory upload
    public void updateUbo() {
        values.put(pointer);
        MemoryUtil.memPutLong(pointer + 188, handles[0]);
        MemoryUtil.memPutLong(pointer + 196, handles[1]);
        MemoryUtil.memPutLong(pointer + 204, handles[2]);
        MemoryUtil.memPutLong(pointer + 212, handles[3]);
    }

    public int getColorMethod() {
        return colorMethod;
    }

    public int[] getEffects() {
        return effect;
    }

    public CullType cullType() {
        return cullType;
    }

    public BlendType blendType() {
        return blendType;
    }

    public long[] handles() {
        return handles;
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
