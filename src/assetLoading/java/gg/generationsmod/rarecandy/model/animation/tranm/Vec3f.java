// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.tranm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class Vec3f extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Vec3f __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public float x() { return bb.getFloat(bb_pos); }
  public float y() { return bb.getFloat(bb_pos + 4); }
  public float z() { return bb.getFloat(bb_pos + 8); }

  public static int createVec3f(FlatBufferBuilder builder, float x, float y, float z) {
    builder.prep(4, 12);
    builder.putFloat(z);
    builder.putFloat(y);
    builder.putFloat(x);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Vec3f get(int j) { return get(new Vec3f(), j); }
    public Vec3f get(Vec3f obj, int j) {  return obj.__assign(__element(j), bb); }
  }
}

