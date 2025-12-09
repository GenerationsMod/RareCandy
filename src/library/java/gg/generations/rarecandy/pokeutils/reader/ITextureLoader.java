package gg.generations.rarecandy.pokeutils.reader;

import gg.generations.rarecandy.renderer.textures.Bindless;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.SamplerDesc;
import gg.generations.rarecandy.renderer.textures.SamplerPresets;

import java.awt.image.BufferedImage;
import java.util.Set;

public abstract class ITextureLoader {
    private static ITextureLoader instance;

    public static ITextureLoader instance() {
        return instance;
    }

    public static void setInstance(ITextureLoader instance) {
        ITextureLoader.instance = instance;
    }

    public abstract ITexture getTexture(String name);

    public abstract void register(String name, ITexture reference);

    public abstract void register(String id, String fileName, byte[] data);

    public abstract void remove(String name);

    public ITexture getDarkFallback() {
        return getTexture("dark");
    }

    public ITexture getBrightFallback() {
        return getTexture("neutral");
    }
    public ITexture getNuetralFallback() {
        return getTexture("bright");
    }

    public abstract Set<String> getTextureEntries();

    /* ───────── Bindless convenience over registry ───────── */

    /**
     * Returns a bindless sampler handle for a named texture using the given sampler description.
     * Throws if the texture is missing or bindless is unavailable on this platform.
     */
    public long getSamplerHandle(String name, SamplerDesc sampler) {
        ITexture tex = getTexture(name);
        if (tex == null) throw new IllegalArgumentException("Unknown texture id: " + name);
        return tex.getSamplerHandle(sampler);
    }

    /**
     * Returns a bindless sampler handle when supported; otherwise binds legacy to the provided unit and returns 0.
     * Useful during staged migration.
     */
    public long getSamplerHandleOrBind(String name, SamplerDesc sampler, int legacyUnit) {
        ITexture tex = getTexture(name);
        if (tex == null) throw new IllegalArgumentException("Unknown texture id: " + name);
        return Bindless.samplerHandleOrBind(tex, sampler, legacyUnit);
    }

    /** Convenience: trilinear repeat handle for the named texture. */
    public long getLinearRepeatHandle(String name) {
        return getSamplerHandle(name, SamplerPresets.TRILINEAR_REPEAT);
    }

    /** Image load/store handle for compute. */
    public long getImageHandle(String name, int level, boolean layered, ITexture.ComputeAccess access) {
        ITexture tex = getTexture(name);
        if (tex == null) throw new IllegalArgumentException("Unknown texture id: " + name);
        return tex.getImageHandle(level, layered, access);
    }
}
