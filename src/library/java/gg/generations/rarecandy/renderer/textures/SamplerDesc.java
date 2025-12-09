package gg.generations.rarecandy.renderer.textures;

import java.util.Objects;

/**
 * Immutable sampler state description; used to cache GL sampler objects and bindless handles.
 */
public final class SamplerDesc {
    public final int minFilter;   // e.g., GL_NEAREST, GL_LINEAR_MIPMAP_LINEAR
    public final int magFilter;   // e.g., GL_NEAREST, GL_LINEAR
    public final int wrapS;       // e.g., GL_REPEAT, GL_CLAMP_TO_EDGE
    public final int wrapT;
    public final int wrapR;       // for 3D/array; unused by 2D but included for key stability
    public final float anisotropy; // 1.0f if unused
    public final float lodBias;    // 0.0f default
    public final float minLod;     // -1000 by default
    public final float maxLod;     // +1000 by default
    public final boolean compareEnable;
    public final int compareFunc;  // GL_LESS, etc. if compareEnable = true
    public final boolean srgbDecodeLinear; // optional policy; if false, rely on sRGB internal formats

    public SamplerDesc(int minFilter, int magFilter, int wrapS, int wrapT, int wrapR,
                       float anisotropy, float lodBias, float minLod, float maxLod,
                       boolean compareEnable, int compareFunc, boolean srgbDecodeLinear) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        this.wrapR = wrapR;
        this.anisotropy = anisotropy;
        this.lodBias = lodBias;
        this.minLod = minLod;
        this.maxLod = maxLod;
        this.compareEnable = compareEnable;
        this.compareFunc = compareFunc;
        this.srgbDecodeLinear = srgbDecodeLinear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SamplerDesc that)) return false;
        return minFilter == that.minFilter && magFilter == that.magFilter &&
               wrapS == that.wrapS && wrapT == that.wrapT && wrapR == that.wrapR &&
               Float.compare(that.anisotropy, anisotropy) == 0 &&
               Float.compare(that.lodBias, lodBias) == 0 &&
               Float.compare(that.minLod, minLod) == 0 &&
               Float.compare(that.maxLod, maxLod) == 0 &&
               compareEnable == that.compareEnable &&
               compareFunc == that.compareFunc &&
               srgbDecodeLinear == that.srgbDecodeLinear;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minFilter, magFilter, wrapS, wrapT, wrapR, anisotropy, lodBias, minLod, maxLod, compareEnable, compareFunc, srgbDecodeLinear);
    }
}