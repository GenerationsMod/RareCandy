// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class sVec2 extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public sVec2 __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public short u() { return bb.getShort(bb_pos + 0); }
  public short v() { return bb.getShort(bb_pos + 2); }

  public static int createsVec2(FlatBufferBuilder builder, short u, short v) {
    builder.prep(2, 4);
    builder.putShort(v);
    builder.putShort(u);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public sVec2 get(int j) { return get(new sVec2(), j); }
    public sVec2 get(sVec2 obj, int j) {  return obj.__assign(__element(j), bb); }
  }
  public sVec2T unpack() {
    sVec2T _o = new sVec2T();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(sVec2T _o) {
    short _oU = u();
    _o.setU(_oU);
    short _oV = v();
    _o.setV(_oV);
  }
  public static int pack(FlatBufferBuilder builder, sVec2T _o) {
    if (_o == null) return 0;
    return createsVec2(
      builder,
      _o.getU(),
      _o.getV());
  }
}

