package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.renderer.model.material.Material;

public enum RenderStage {
    SOLID_DEPTH_CULL(false, true, true),
    SOLID_DEPTH_NOCULL(false, true, false),
    SOLID_NODEPTH_CULL(false, false, true),
    SOLID_NODEPTH_NOCULL(false, false, false),
    TRANSPARENT_DEPTH_CULL(true, true, true),
    TRANSPARENT_DEPTH_NOCULL(true, true, false),
    TRANSPARENT_NODEPTH_CULL(true, false, true),
    TRANSPARENT_NODEPTH_NOCULL(true, false, false);

    private final boolean depthTest;
    private final boolean blend;
    private final boolean cull;

    RenderStage(boolean blend, boolean depthTest, boolean cull) {
        this.depthTest = depthTest;
        this.blend = blend;
        this.cull = cull;
    }

    public static RenderStage from(Material material) {
        return from(material.blendType() == BlendType.Regular, !material.disableDepth(), material.cullType() != CullType.None);
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public boolean isBlend() {
        return blend;
    }

    public boolean isCull() {
        return cull;
    }

    public static RenderStage from(boolean blend, boolean depthTest, boolean cull) {
        if(blend) {
            if(depthTest) {
                if (cull) return TRANSPARENT_DEPTH_CULL;
                else return TRANSPARENT_DEPTH_NOCULL;
            } else {
                if (cull) return TRANSPARENT_NODEPTH_CULL;
                else return TRANSPARENT_NODEPTH_NOCULL;
            }
        } else {
            if(depthTest) {
                if (cull) return SOLID_DEPTH_CULL;
                else return SOLID_DEPTH_NOCULL;
            } else {
                if (cull) return SOLID_NODEPTH_CULL;
                else return SOLID_NODEPTH_NOCULL;
            }
        }
    }
}
