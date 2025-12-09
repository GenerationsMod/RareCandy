package gg.generations.rarecandy.renderer.textures;

/**
 * Helper methods that prefer bindless; fall back to legacy texture unit binding when unsupported.
 * These do not alter existing code paths; call sites can opt-in while legacy users keep using bind().
 */
public final class Bindless {
    private Bindless() {
    }

    /**
     * Returns a bindless sampler handle when available; otherwise binds to the provided unit and returns 0.
     * Callers can branch on (handle != 0L) to choose shader path.
     */
    public static long samplerHandleOrBind(ITexture tex, SamplerDesc sampler, int unit) {
        if (BindlessSupport.isSupported()) {
            return tex.getSamplerHandle(sampler);
        } else {
            tex.bind(unit);
            return 0L;
        }
    }

    /**
     * Returns a bindless image handle when available; otherwise binds for image load/store on the given unit
     * (callers still need glBindImageTexture themselves if they choose to support legacy).
     * For strict separation, prefer using bindless-only compute paths and treat 0 as "no-bindless".
     */
    public static long imageHandleOrZero(ITexture tex, int level, boolean layered, ITexture.ComputeAccess access) {
        if (BindlessSupport.isSupported()) {
            return tex.getImageHandle(level, layered, access);
        }
        return 0L;
    }

    /**
     * Hard fail helper when a bindless path is strictly required.
     */
    public static long requireSamplerHandle(ITexture tex, SamplerDesc sampler) {
        return tex.getSamplerHandle(sampler);
    }
}