package gg.generations.rarecandy.renderer.textures;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_REPEAT;

/**
 * Common immutable sampler descriptors for convenience.
 * Use these as keys into {@link SamplerCache}.
 */
public final class SamplerPresets {
    private SamplerPresets() {}

    /** Nearest, no mips, repeat. */
    public static final SamplerDesc NEAREST_REPEAT = new SamplerDesc(
            GL_NEAREST, GL_NEAREST, GL_REPEAT, GL_REPEAT, GL_REPEAT,
+            1.0f, 0.0f, -1000f, 1000f, false, GL_LESS, true
    );

    /** Linear, no mips, clamp. */
    public static final SamplerDesc LINEAR_CLAMP = new SamplerDesc(
            GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE,
+            1.0f, 0.0f, -1000f, 1000f, false, GL_LESS, true
    );

    /** Linear with mipmaps, repeat. */
    public static final SamplerDesc TRILINEAR_REPEAT = new SamplerDesc(
            GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_REPEAT, GL_REPEAT, GL_REPEAT,
+            1.0f, 0.0f, -1000f, 1000f, false, GL_LESS, true
    );

    /** Linear with mipmaps, clamp. */
    public static final SamplerDesc TRILINEAR_CLAMP = new SamplerDesc(
            GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE,
+            1.0f, 0.0f, -1000f, 1000f, false, GL_LESS, true
    );

    /** Shadow compare sampler, clamp. */
    public static final SamplerDesc SHADOW_CLAMP = new SamplerDesc(
            GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE,
+            1.0f, 0.0f, -1000f, 1000f, true, GL_LESS, true
    );
}