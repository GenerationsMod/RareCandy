// automatically generated by the FlatBuffers compiler, do not modify

package com.pokemod.pokeutils.tranm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class DynamicQuatTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_1_21(); }
  public static DynamicQuatTrack getRootAsDynamicQuatTrack(ByteBuffer _bb) { return getRootAsDynamicQuatTrack(_bb, new DynamicQuatTrack()); }
  public static DynamicQuatTrack getRootAsDynamicQuatTrack(ByteBuffer _bb, DynamicQuatTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public DynamicQuatTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public Vec3s vec(int j) { return vec(new Vec3s(), j); }
  public Vec3s vec(Vec3s obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o) + j * 6, bb) : null; }
  public int vecLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public Vec3s.Vector vecVector() { return vecVector(new Vec3s.Vector()); }
  public Vec3s.Vector vecVector(Vec3s.Vector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), 6, bb) : null; }

  public static int createDynamicQuatTrack(FlatBufferBuilder builder,
      int vecOffset) {
    builder.startTable(1);
    DynamicQuatTrack.addVec(builder, vecOffset);
    return DynamicQuatTrack.endDynamicQuatTrack(builder);
  }

  public static void startDynamicQuatTrack(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addVec(FlatBufferBuilder builder, int vecOffset) { builder.addOffset(0, vecOffset, 0); }
  public static void startVecVector(FlatBufferBuilder builder, int numElems) { builder.startVector(6, numElems, 2); }
  public static int endDynamicQuatTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public DynamicQuatTrack get(int j) { return get(new DynamicQuatTrack(), j); }
    public DynamicQuatTrack get(DynamicQuatTrack obj, int j) {  return obj.__assign(Table.__indirect(__element(j), bb), bb); }
  }
}

