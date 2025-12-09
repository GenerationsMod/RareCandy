package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.GL;

/**
 * Feature gate for ARB_bindless_texture.
 */
public final class BindlessSupport {
    private static Boolean supported;

    private BindlessSupport() {}

    public static boolean isSupported() {
        if (supported == null) {
            supported = GL.getCapabilities().GL_ARB_bindless_texture;
        }
        return supported;
    }

    public static void require() {
        if (!isSupported()) {
            throw new UnsupportedOperationException("ARB_bindless_texture is required but not available on this context");
        }
    }
}