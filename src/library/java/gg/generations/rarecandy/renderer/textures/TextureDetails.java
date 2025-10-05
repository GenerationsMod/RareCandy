package gg.generations.rarecandy.renderer.textures;

public interface TextureDetails extends AutoCloseable {
    int init();

    int width();

    int height();

    ITexture.Type type();
}
