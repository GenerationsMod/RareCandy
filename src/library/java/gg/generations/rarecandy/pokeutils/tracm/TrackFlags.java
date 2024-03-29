// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.tracm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TrackFlags extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static TrackFlags getRootAsTrackFlags(ByteBuffer _bb) { return getRootAsTrackFlags(_bb, new TrackFlags()); }
  public static TrackFlags getRootAsTrackFlags(ByteBuffer _bb, TrackFlags obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public TrackFlags __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long res0() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long res1() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public TrackFlagsInfo info() { return info(new TrackFlagsInfo()); }
  public TrackFlagsInfo info(TrackFlagsInfo obj) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }

  public static int createTrackFlags(FlatBufferBuilder builder,
      long res0,
      long res1,
      int infoOffset) {
    builder.startTable(3);
    TrackFlags.addInfo(builder, infoOffset);
    TrackFlags.addRes1(builder, res1);
    TrackFlags.addRes0(builder, res0);
    return TrackFlags.endTrackFlags(builder);
  }

  public static void startTrackFlags(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addRes0(FlatBufferBuilder builder, long res0) { builder.addInt(0, (int) res0, (int) 0L); }
  public static void addRes1(FlatBufferBuilder builder, long res1) { builder.addInt(1, (int) res1, (int) 0L); }
  public static void addInfo(FlatBufferBuilder builder, int infoOffset) { builder.addOffset(2, infoOffset, 0); }
  public static int endTrackFlags(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public TrackFlags get(int j) { return get(new TrackFlags(), j); }
    public TrackFlags get(TrackFlags obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

