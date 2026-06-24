package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.TransformSet;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;

import java.nio.ByteBuffer;

public record Variant(int material, int effect, boolean paradox, boolean hide, ITransform[] transform) {
    public static final int SIZE = TransformSet.SIZE + Integer.BYTES * 4;

    public void put(SSBOBuffer buffer) {
        transform[0].upload(buffer);
        transform[1].upload(buffer);
        transform[2].upload(buffer);
        transform[3].upload(buffer);
        buffer.put(material);
        buffer.put(effect);
        buffer.put(paradox);
        buffer.put(0);
    }
}
