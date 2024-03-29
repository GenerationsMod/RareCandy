// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks.data;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class StringDataTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static StringDataTrack getRootAsStringDataTrack(ByteBuffer _bb) { return getRootAsStringDataTrack(_bb, new StringDataTrack()); }
  public static StringDataTrack getRootAsStringDataTrack(ByteBuffer _bb, StringDataTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public StringDataTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String value() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer valueAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer valueInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }

  public static int createStringDataTrack(FlatBufferBuilder builder,
      int valueOffset) {
    builder.startTable(1);
    StringDataTrack.addValue(builder, valueOffset);
    return StringDataTrack.endStringDataTrack(builder);
  }

  public static void startStringDataTrack(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addValue(FlatBufferBuilder builder, int valueOffset) { builder.addOffset(0, valueOffset, 0); }
  public static int endStringDataTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public StringDataTrack get(int j) { return get(new StringDataTrack(), j); }
    public StringDataTrack get(StringDataTrack obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public StringDataTrackT unpack() {
    StringDataTrackT _o = new StringDataTrackT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(StringDataTrackT _o) {
    String _oValue = value();
    _o.setValue(_oValue);
  }
  public static int pack(FlatBufferBuilder builder, StringDataTrackT _o) {
    if (_o == null) return 0;
    int _value = _o.getValue() == null ? 0 : builder.createString(_o.getValue());
    return createStringDataTrack(
      builder,
      _value);
  }
}

