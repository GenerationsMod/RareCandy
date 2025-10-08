package gg.generations.rarecandy.renderer.loading;

import java.nio.ByteBuffer;

public record PackedVertex(
        short qx,
        short qy,
        short qz,
        int uvPacked,
        int packedNormal,
        int boneIdPacked,
        int boneWeightPacked) {
    public void put(ByteBuffer buffer) {
        buffer.putShort(qx);
        buffer.putShort(qy);
        buffer.putShort(qz);
        buffer.putShort((short) 0);
        buffer.putInt(uvPacked);
        buffer.putInt(packedNormal);
        buffer.putInt(boneIdPacked);
        buffer.putInt(boneWeightPacked);
    }
}
