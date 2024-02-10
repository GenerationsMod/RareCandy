// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.tracm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TrackMaterialValue extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static TrackMaterialValue getRootAsTrackMaterialValue(ByteBuffer _bb) { return getRootAsTrackMaterialValue(_bb, new TrackMaterialValue()); }
  public static TrackMaterialValue getRootAsTrackMaterialValue(ByteBuffer _bb, TrackMaterialValue obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public TrackMaterialValue __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public float time() { int o = __offset(4); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public float value() { int o = __offset(6); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public long config0() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long config1() { int o = __offset(10); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long config2() { int o = __offset(12); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createTrackMaterialValue(FlatBufferBuilder builder,
      float time,
      float value,
      long config0,
      long config1,
      long config2) {
    builder.startTable(5);
    TrackMaterialValue.addConfig2(builder, config2);
    TrackMaterialValue.addConfig1(builder, config1);
    TrackMaterialValue.addConfig0(builder, config0);
    TrackMaterialValue.addValue(builder, value);
    TrackMaterialValue.addTime(builder, time);
    return TrackMaterialValue.endTrackMaterialValue(builder);
  }

  public static void startTrackMaterialValue(FlatBufferBuilder builder) { builder.startTable(5); }
  public static void addTime(FlatBufferBuilder builder, float time) { builder.addFloat(0, time, 0.0f); }
  public static void addValue(FlatBufferBuilder builder, float value) { builder.addFloat(1, value, 0.0f); }
  public static void addConfig0(FlatBufferBuilder builder, long config0) { builder.addInt(2, (int) config0, (int) 0L); }
  public static void addConfig1(FlatBufferBuilder builder, long config1) { builder.addInt(3, (int) config1, (int) 0L); }
  public static void addConfig2(FlatBufferBuilder builder, long config2) { builder.addInt(4, (int) config2, (int) 0L); }
  public static int endTrackMaterialValue(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public TrackMaterialValue get(int j) { return get(new TrackMaterialValue(), j); }
    public TrackMaterialValue get(TrackMaterialValue obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

