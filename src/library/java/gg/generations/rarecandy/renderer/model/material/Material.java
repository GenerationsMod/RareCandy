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

    public void put(ByteBuffer buffer) {
        buffer.putInt(images[0]);
        buffer.putInt(images[1]);
        buffer.putInt(images[2]);
        buffer.putInt(images[3]);
        values.put(buffer);
        buffer.putInt(colorMethod);
        buffer.putInt(blendType() == BlendType.Regular ? 1 : 0);
    }

    @Override
    public void close() {
    }

    public boolean disableDepth() {
        return values().getDisableDepth();
    }
}
