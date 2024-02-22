package gg.generations.rarecandy.legacy.pipeline;

import java.io.Closeable;

public interface ITexture extends Closeable {
    void bind(int slot);
}
