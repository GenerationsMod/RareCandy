package gg.generations.rarecandy.renderer.loading;

import java.io.Closeable;

public interface ITexture extends Closeable {
    void bind(int slot);

    int width();
    int height();
}
