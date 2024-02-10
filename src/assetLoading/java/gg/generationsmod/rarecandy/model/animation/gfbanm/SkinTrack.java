// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.gfbanm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class SkinTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static SkinTrack getRootAsSkinTrack(ByteBuffer _bb) { return getRootAsSkinTrack(_bb, new SkinTrack()); }
  public static SkinTrack getRootAsSkinTrack(ByteBuffer _bb, SkinTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public SkinTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public byte valuesType() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table values(Table obj) { int o = __offset(8); return o != 0 ? __union(obj, o + bb_pos) : null; }

  public static int createSkinTrack(FlatBufferBuilder builder,
      int nameOffset,
      byte valuesType,
      int valuesOffset) {
    builder.startTable(3);
    SkinTrack.addValues(builder, valuesOffset);
    SkinTrack.addName(builder, nameOffset);
    SkinTrack.addValuesType(builder, valuesType);
    return SkinTrack.endSkinTrack(builder);
  }

  public static void startSkinTrack(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addValuesType(FlatBufferBuilder builder, byte valuesType) { builder.addByte(1, valuesType, 0); }
  public static void addValues(FlatBufferBuilder builder, int valuesOffset) { builder.addOffset(2, valuesOffset, 0); }
  public static int endSkinTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public SkinTrack get(int j) { return get(new SkinTrack(), j); }
    public SkinTrack get(SkinTrack obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public SkinTrackT unpack() {
    SkinTrackT _o = new SkinTrackT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(SkinTrackT _o) {
    String _oName = name();
    _o.setName(_oName);
    ByteTrackUnion _oValues = new ByteTrackUnion();
    byte _oValuesType = valuesType();
    _oValues.setType(_oValuesType);
    Table _oValuesValue;
    switch (_oValuesType) {
      case ByteTrack.FixedByteTrack:
        _oValuesValue = values(new FixedByteTrack());
        _oValues.setValue(_oValuesValue != null ? ((FixedByteTrack) _oValuesValue).unpack() : null);
        break;
      case ByteTrack.DynamicByteTrack:
        _oValuesValue = values(new DynamicByteTrack());
        _oValues.setValue(_oValuesValue != null ? ((DynamicByteTrack) _oValuesValue).unpack() : null);
        break;
      case ByteTrack.Framed16ByteTrack:
        _oValuesValue = values(new Framed16ByteTrack());
        _oValues.setValue(_oValuesValue != null ? ((Framed16ByteTrack) _oValuesValue).unpack() : null);
        break;
      case ByteTrack.Framed8ByteTrack:
        _oValuesValue = values(new Framed8ByteTrack());
        _oValues.setValue(_oValuesValue != null ? ((Framed8ByteTrack) _oValuesValue).unpack() : null);
        break;
      default: break;
    }
    _o.setValues(_oValues);
  }
  public static int pack(FlatBufferBuilder builder, SkinTrackT _o) {
    if (_o == null) return 0;
    int _name = _o.getName() == null ? 0 : builder.createString(_o.getName());
    byte _valuesType = _o.getValues() == null ? ByteTrack.NONE : _o.getValues().getType();
    int _values = _o.getValues() == null ? 0 : ByteTrackUnion.pack(builder, _o.getValues());
    return createSkinTrack(
      builder,
      _name,
      _valuesType,
      _values);
  }
}

