package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;

public final class TextureFactory {
    private TextureFactory() {
    }

    public static ITexture createTexture2D(int width, int height, ITexture.Type type) {
        return new BlankTexture(type, width, height, ITexture.ComputeAccess.READ_ONLY);
    }
}
