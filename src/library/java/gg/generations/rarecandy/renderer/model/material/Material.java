package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;

import java.io.Closeable;
import java.nio.ByteBuffer;

public record Material(
        int[] images,
        IMaterialValues values,
        CullType cullType,
        BlendType blendType,
        int colorMethod) implements Closeable {

    public void put(SSBOBuffer buffer) {
        buffer.put(images[0]);
        buffer.put(images[1]);
        buffer.put(images[2]);
        buffer.put(images[3]);
        values.put(buffer);
        buffer.put(colorMethod);
        buffer.put(blendType() == BlendType.Regular ? 1 : 0);
    }

    @Override
    public void close() {
    }

    public boolean disableDepth() {
        return values().disableDepth();
    }
}
