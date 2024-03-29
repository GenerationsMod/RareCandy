// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class Vec3 extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Vec3 __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public float x() { return bb.getFloat(bb_pos + 0); }
  public float y() { return bb.getFloat(bb_pos + 4); }
  public float z() { return bb.getFloat(bb_pos + 8); }

  public static int createVec3(FlatBufferBuilder builder, float x, float y, float z) {
    builder.prep(4, 12);
    builder.putFloat(z);
    builder.putFloat(y);
    builder.putFloat(x);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Vec3 get(int j) { return get(new Vec3(), j); }
    public Vec3 get(Vec3 obj, int j) {  return obj.__assign(__element(j), bb); }
  }
  public Vec3T unpack() {
    Vec3T _o = new Vec3T();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(Vec3T _o) {
    float _oX = x();
    _o.setX(_oX);
    float _oY = y();
    _o.setY(_oY);
    float _oZ = z();
    _o.setZ(_oZ);
  }
  public static int pack(FlatBufferBuilder builder, Vec3T _o) {
    if (_o == null) return 0;
    return createVec3(
      builder,
      _o.getX(),
      _o.getY(),
      _o.getZ());
  }
}

