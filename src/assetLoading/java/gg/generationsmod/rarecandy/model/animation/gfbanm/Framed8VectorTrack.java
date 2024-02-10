// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.gfbanm;

import com.google.flatbuffers.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Framed8VectorTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Framed8VectorTrack getRootAsFramed8VectorTrack(ByteBuffer _bb) { return getRootAsFramed8VectorTrack(_bb, new Framed8VectorTrack()); }
  public static Framed8VectorTrack getRootAsFramed8VectorTrack(ByteBuffer _bb, Framed8VectorTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Framed8VectorTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int frames(int j) { int o = __offset(4); return o != 0 ? bb.get(__vector(o) + j * 1) & 0xFF : 0; }
  public int framesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ByteVector framesVector() { return framesVector(new ByteVector()); }
  public ByteVector framesVector(ByteVector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer framesAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer framesInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public Vec3 co(int j) { return co(new Vec3(), j); }
  public Vec3 co(Vec3 obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o) + j * 12, bb) : null; }
  public int coLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public Vec3.Vector coVector() { return coVector(new Vec3.Vector()); }
  public Vec3.Vector coVector(Vec3.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 12, bb) : null; }

  public static int createFramed8VectorTrack(FlatBufferBuilder builder,
      int framesOffset,
      int coOffset) {
    builder.startTable(2);
    Framed8VectorTrack.addCo(builder, coOffset);
    Framed8VectorTrack.addFrames(builder, framesOffset);
    return Framed8VectorTrack.endFramed8VectorTrack(builder);
  }

  public static void startFramed8VectorTrack(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addFrames(FlatBufferBuilder builder, int framesOffset) { builder.addOffset(0, framesOffset, 0); }
  public static int createFramesVector(FlatBufferBuilder builder, byte[] data) { return builder.createByteVector(data); }
  public static int createFramesVector(FlatBufferBuilder builder, ByteBuffer data) { return builder.createByteVector(data); }
  public static void startFramesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static void addCo(FlatBufferBuilder builder, int coOffset) { builder.addOffset(1, coOffset, 0); }
  public static void startCoVector(FlatBufferBuilder builder, int numElems) { builder.startVector(12, numElems, 4); }
  public static int endFramed8VectorTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Framed8VectorTrack get(int j) { return get(new Framed8VectorTrack(), j); }
    public Framed8VectorTrack get(Framed8VectorTrack obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public Framed8VectorTrackT unpack() {
    Framed8VectorTrackT _o = new Framed8VectorTrackT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(Framed8VectorTrackT _o) {
    int[] _oFrames = new int[framesLength()];
    for (int _j = 0; _j < framesLength(); ++_j) {_oFrames[_j] = frames(_j);}
    _o.setFrames(_oFrames);
    Vec3T[] _oCo = new Vec3T[coLength()];
    for (int _j = 0; _j < coLength(); ++_j) {_oCo[_j] = (co(_j) != null ? co(_j).unpack() : null);}
    _o.setCo(_oCo);
  }
  public static int pack(FlatBufferBuilder builder, Framed8VectorTrackT _o) {
    if (_o == null) return 0;
    int _frames = 0;
    if (_o.getFrames() != null) {
      byte[] __frames = new byte[_o.getFrames().length];
      int _j = 0;
      for (int _e : _o.getFrames()) { __frames[_j] = (byte) _e; _j++;}
      _frames = createFramesVector(builder, __frames);
    }
    int _co = 0;
    Vec3T[] _oCo = _o.getCo();
    if (_oCo != null) {
      int _unused_offset = 0;
      startCoVector(builder, _oCo.length);
      for (int _j = _oCo.length - 1; _j >=0; _j--) { _unused_offset = Vec3.pack(builder, _oCo[_j]);}
      _co = builder.endVector();
    }
    return createFramed8VectorTrack(
      builder,
      _frames,
      _co);
  }
}

