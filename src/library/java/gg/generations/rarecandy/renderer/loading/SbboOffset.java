package gg.generations.rarecandy.renderer.loading;

import java.nio.ByteBuffer;

public record SbboOffset(int base, int size) {
    public void put(ByteBuffer target, ByteBuffer src) {
        target.put(base, src, 0, size);
    }
}
