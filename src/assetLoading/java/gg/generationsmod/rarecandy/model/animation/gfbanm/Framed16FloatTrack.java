// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.gfbanm;

import com.google.flatbuffers.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Framed16FloatTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Framed16FloatTrack getRootAsFramed16FloatTrack(ByteBuffer _bb) { return getRootAsFramed16FloatTrack(_bb, new Framed16FloatTrack()); }
  public static Framed16FloatTrack getRootAsFramed16FloatTrack(ByteBuffer _bb, Framed16FloatTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Framed16FloatTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int frames(int j) { int o = __offset(4); return o != 0 ? bb.getShort(__vector(o) + j * 2) & 0xFFFF : 0; }
  public int framesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ShortVector framesVector() { return framesVector(new ShortVector()); }
  public ShortVector framesVector(ShortVector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer framesAsByteBuffer() { return __vector_as_bytebuffer(4, 2); }
  public ByteBuffer framesInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 2); }
  public float float_(int j) { int o = __offset(6); return o != 0 ? bb.getFloat(__vector(o) + j * 4) : 0; }
  public int float_Length() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public FloatVector floatVector() { return floatVector(new FloatVector()); }
  public FloatVector floatVector(FloatVector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer float_AsByteBuffer() { return __vector_as_bytebuffer(6, 4); }
  public ByteBuffer float_InByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 4); }

  public static int createFramed16FloatTrack(FlatBufferBuilder builder,
      int framesOffset,
      int float_Offset) {
    builder.startTable(2);
    Framed16FloatTrack.addFloat(builder, float_Offset);
    Framed16FloatTrack.addFrames(builder, framesOffset);
    return Framed16FloatTrack.endFramed16FloatTrack(builder);
  }

  public static void startFramed16FloatTrack(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addFrames(FlatBufferBuilder builder, int framesOffset) { builder.addOffset(0, framesOffset, 0); }
  public static int createFramesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(2, data.length, 2); for (int i = data.length - 1; i >= 0; i--) builder.addShort((short) data[i]); return builder.endVector(); }
  public static void startFramesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(2, numElems, 2); }
  public static void addFloat(FlatBufferBuilder builder, int float_Offset) { builder.addOffset(1, float_Offset, 0); }
  public static int createFloatVector(FlatBufferBuilder builder, float[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addFloat(data[i]); return builder.endVector(); }
  public static void startFloatVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endFramed16FloatTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Framed16FloatTrack get(int j) { return get(new Framed16FloatTrack(), j); }
    public Framed16FloatTrack get(Framed16FloatTrack obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public Framed16FloatTrackT unpack() {
    Framed16FloatTrackT _o = new Framed16FloatTrackT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(Framed16FloatTrackT _o) {
    int[] _oFrames = new int[framesLength()];
    for (int _j = 0; _j < framesLength(); ++_j) {_oFrames[_j] = frames(_j);}
    _o.setFrames(_oFrames);
    float[] _oFloat = new float[float_Length()];
    for (int _j = 0; _j < float_Length(); ++_j) {_oFloat[_j] = float_(_j);}
    _o.setFloat(_oFloat);
  }
  public static int pack(FlatBufferBuilder builder, Framed16FloatTrackT _o) {
    if (_o == null) return 0;
    int _frames = 0;
    if (_o.getFrames() != null) {
      _frames = createFramesVector(builder, _o.getFrames());
    }
    int _float_ = 0;
    if (_o.getFloat() != null) {
      _float_ = createFloatVector(builder, _o.getFloat());
    }
    return createFramed16FloatTrack(
      builder,
      _frames,
      _float_);
  }
}

