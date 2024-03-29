// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class Transform extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Transform __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public Vec3 scale() { return scale(new Vec3()); }
  public Vec3 scale(Vec3 obj) { return obj.__assign(bb_pos + 0, bb); }
  public Vec4 rotate() { return rotate(new Vec4()); }
  public Vec4 rotate(Vec4 obj) { return obj.__assign(bb_pos + 12, bb); }
  public Vec3 translate() { return translate(new Vec3()); }
  public Vec3 translate(Vec3 obj) { return obj.__assign(bb_pos + 28, bb); }

  public static int createTransform(FlatBufferBuilder builder, float scale_x, float scale_y, float scale_z, float rotate_x, float rotate_y, float rotate_z, float rotate_w, float translate_x, float translate_y, float translate_z) {
    builder.prep(4, 40);
    builder.prep(4, 12);
    builder.putFloat(translate_z);
    builder.putFloat(translate_y);
    builder.putFloat(translate_x);
    builder.prep(4, 16);
    builder.putFloat(rotate_w);
    builder.putFloat(rotate_z);
    builder.putFloat(rotate_y);
    builder.putFloat(rotate_x);
    builder.prep(4, 12);
    builder.putFloat(scale_z);
    builder.putFloat(scale_y);
    builder.putFloat(scale_x);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Transform get(int j) { return get(new Transform(), j); }
    public Transform get(Transform obj, int j) {  return obj.__assign(__element(j), bb); }
  }
  public TransformT unpack() {
    TransformT _o = new TransformT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(TransformT _o) {
    scale().unpackTo(_o.getScale());
    rotate().unpackTo(_o.getRotate());
    translate().unpackTo(_o.getTranslate());
  }
  public static int pack(FlatBufferBuilder builder, TransformT _o) {
    if (_o == null) return 0;
    float _scale_x = _o.getScale().getX();
    float _scale_y = _o.getScale().getY();
    float _scale_z = _o.getScale().getZ();
    float _rotate_x = _o.getRotate().getX();
    float _rotate_y = _o.getRotate().getY();
    float _rotate_z = _o.getRotate().getZ();
    float _rotate_w = _o.getRotate().getW();
    float _translate_x = _o.getTranslate().getX();
    float _translate_y = _o.getTranslate().getY();
    float _translate_z = _o.getTranslate().getZ();
    return createTransform(
      builder,
      _scale_x,
      _scale_y,
      _scale_z,
      _rotate_x,
      _rotate_y,
      _rotate_z,
      _rotate_w,
      _translate_x,
      _translate_y,
      _translate_z);
  }
}

