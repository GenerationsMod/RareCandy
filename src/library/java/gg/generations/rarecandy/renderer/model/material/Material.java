package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;

import java.io.Closeable;
import java.nio.ByteBuffer;

public record Material(
        int[] images,
        MaterialValues values,
        CullType cullType,
        BlendType blendType,
        int colorMethod) implements Closeable {

    public void  put(ByteBuffer buffer) {
        values.put(buffer);
        buffer.putInt(colorMethod);
        buffer.putInt(0);
        buffer.putInt(0);
    }

    @Override
    public void close() {
    }

    public boolean disableDepth() {
        return values().getDisableDepth();
    }
}
