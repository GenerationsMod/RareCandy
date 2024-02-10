// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.gfbanm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Info extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Info getRootAsInfo(ByteBuffer _bb) { return getRootAsInfo(_bb, new Info()); }
  public static Info getRootAsInfo(ByteBuffer _bb, Info obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Info __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long doesLoop() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long keyFrames() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long frameRate() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createInfo(FlatBufferBuilder builder,
      long doesLoop,
      long keyFrames,
      long frameRate) {
    builder.startTable(3);
    Info.addFrameRate(builder, frameRate);
    Info.addKeyFrames(builder, keyFrames);
    Info.addDoesLoop(builder, doesLoop);
    return Info.endInfo(builder);
  }

  public static void startInfo(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addDoesLoop(FlatBufferBuilder builder, long doesLoop) { builder.addInt(0, (int) doesLoop, (int) 0L); }
  public static void addKeyFrames(FlatBufferBuilder builder, long keyFrames) { builder.addInt(1, (int) keyFrames, (int) 0L); }
  public static void addFrameRate(FlatBufferBuilder builder, long frameRate) { builder.addInt(2, (int) frameRate, (int) 0L); }
  public static int endInfo(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Info get(int j) { return get(new Info(), j); }
    public Info get(Info obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public InfoT unpack() {
    InfoT _o = new InfoT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(InfoT _o) {
    long _oDoesLoop = doesLoop();
    _o.setDoesLoop(_oDoesLoop);
    long _oKeyFrames = keyFrames();
    _o.setKeyFrames(_oKeyFrames);
    long _oFrameRate = frameRate();
    _o.setFrameRate(_oFrameRate);
  }
  public static int pack(FlatBufferBuilder builder, InfoT _o) {
    if (_o == null) return 0;
    return createInfo(
      builder,
      _o.getDoesLoop(),
      _o.getKeyFrames(),
      _o.getFrameRate());
  }
}
