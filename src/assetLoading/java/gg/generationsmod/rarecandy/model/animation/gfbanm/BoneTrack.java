// automatically generated by the FlatBuffers compiler, do not modify

package gg.generationsmod.rarecandy.model.animation.gfbanm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class BoneTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static BoneTrack getRootAsBoneTrack(ByteBuffer _bb) { return getRootAsBoneTrack(_bb, new BoneTrack()); }
  public static BoneTrack getRootAsBoneTrack(ByteBuffer _bb, BoneTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public BoneTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public byte scaleType() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table scale(Table obj) { int o = __offset(8); return o != 0 ? __union(obj, o + bb_pos) : null; }
  public byte rotateType() { int o = __offset(10); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table rotate(Table obj) { int o = __offset(12); return o != 0 ? __union(obj, o + bb_pos) : null; }
  public byte translateType() { int o = __offset(14); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table translate(Table obj) { int o = __offset(16); return o != 0 ? __union(obj, o + bb_pos) : null; }

  public static int createBoneTrack(FlatBufferBuilder builder,
      int nameOffset,
      byte scaleType,
      int scaleOffset,
      byte rotateType,
      int rotateOffset,
      byte translateType,
      int translateOffset) {
    builder.startTable(7);
    BoneTrack.addTranslate(builder, translateOffset);
    BoneTrack.addRotate(builder, rotateOffset);
    BoneTrack.addScale(builder, scaleOffset);
    BoneTrack.addName(builder, nameOffset);
    BoneTrack.addTranslateType(builder, translateType);
    BoneTrack.addRotateType(builder, rotateType);
    BoneTrack.addScaleType(builder, scaleType);
    return BoneTrack.endBoneTrack(builder);
  }

  public static void startBoneTrack(FlatBufferBuilder builder) { builder.startTable(7); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addScaleType(FlatBufferBuilder builder, byte scaleType) { builder.addByte(1, scaleType, 0); }
  public static void addScale(FlatBufferBuilder builder, int scaleOffset) { builder.addOffset(2, scaleOffset, 0); }
  public static void addRotateType(FlatBufferBuilder builder, byte rotateType) { builder.addByte(3, rotateType, 0); }
  public static void addRotate(FlatBufferBuilder builder, int rotateOffset) { builder.addOffset(4, rotateOffset, 0); }
  public static void addTranslateType(FlatBufferBuilder builder, byte translateType) { builder.addByte(5, translateType, 0); }
  public static void addTranslate(FlatBufferBuilder builder, int translateOffset) { builder.addOffset(6, translateOffset, 0); }
  public static int endBoneTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public BoneTrack get(int j) { return get(new BoneTrack(), j); }
    public BoneTrack get(BoneTrack obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public BoneTrackT unpack() {
    BoneTrackT _o = new BoneTrackT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(BoneTrackT _o) {
    String _oName = name();
    _o.setName(_oName);
    VectorTrackUnion _oScale = new VectorTrackUnion();
    byte _oScaleType = scaleType();
    _oScale.setType(_oScaleType);
    Table _oScaleValue;
    switch (_oScaleType) {
      case VectorTrack.FixedVectorTrack:
        _oScaleValue = scale(new FixedVectorTrack());
        _oScale.setValue(_oScaleValue != null ? ((FixedVectorTrack) _oScaleValue).unpack() : null);
        break;
      case VectorTrack.DynamicVectorTrack:
        _oScaleValue = scale(new DynamicVectorTrack());
        _oScale.setValue(_oScaleValue != null ? ((DynamicVectorTrack) _oScaleValue).unpack() : null);
        break;
      case VectorTrack.Framed16VectorTrack:
        _oScaleValue = scale(new Framed16VectorTrack());
        _oScale.setValue(_oScaleValue != null ? ((Framed16VectorTrack) _oScaleValue).unpack() : null);
        break;
      case VectorTrack.Framed8VectorTrack:
        _oScaleValue = scale(new Framed8VectorTrack());
        _oScale.setValue(_oScaleValue != null ? ((Framed8VectorTrack) _oScaleValue).unpack() : null);
        break;
      default: break;
    }
    _o.setScale(_oScale);
    RotationTrackUnion _oRotate = new RotationTrackUnion();
    byte _oRotateType = rotateType();
    _oRotate.setType(_oRotateType);
    Table _oRotateValue;
    switch (_oRotateType) {
      case RotationTrack.FixedRotationTrack -> {
        _oRotateValue = rotate(new FixedRotationTrack());
        _oRotate.setValue(_oRotateValue != null ? ((FixedRotationTrack) _oRotateValue).unpack() : null);
      }
      case RotationTrack.DynamicRotationTrack -> {
        _oRotateValue = rotate(new DynamicRotationTrack());
        _oRotate.setValue(_oRotateValue != null ? ((DynamicRotationTrack) _oRotateValue).unpack() : null);
      }
      case RotationTrack.Framed16RotationTrack -> {
        _oRotateValue = rotate(new Framed16RotationTrack());
        _oRotate.setValue(_oRotateValue != null ? ((Framed16RotationTrack) _oRotateValue).unpack() : null);
      }
      case RotationTrack.Framed8RotationTrack -> {
        _oRotateValue = rotate(new Framed8RotationTrack());
        _oRotate.setValue(_oRotateValue != null ? ((Framed8RotationTrack) _oRotateValue).unpack() : null);
      }
      default -> {
      }
    }
    _o.setRotate(_oRotate);
    VectorTrackUnion _oTranslate = new VectorTrackUnion();
    byte _oTranslateType = translateType();
    _oTranslate.setType(_oTranslateType);
    Table _oTranslateValue;
    switch (_oTranslateType) {
      case VectorTrack.FixedVectorTrack:
        _oTranslateValue = translate(new FixedVectorTrack());
        _oTranslate.setValue(_oTranslateValue != null ? ((FixedVectorTrack) _oTranslateValue).unpack() : null);
        break;
      case VectorTrack.DynamicVectorTrack:
        _oTranslateValue = translate(new DynamicVectorTrack());
        _oTranslate.setValue(_oTranslateValue != null ? ((DynamicVectorTrack) _oTranslateValue).unpack() : null);
        break;
      case VectorTrack.Framed16VectorTrack:
        _oTranslateValue = translate(new Framed16VectorTrack());
        _oTranslate.setValue(_oTranslateValue != null ? ((Framed16VectorTrack) _oTranslateValue).unpack() : null);
        break;
      case VectorTrack.Framed8VectorTrack:
        _oTranslateValue = translate(new Framed8VectorTrack());
        _oTranslate.setValue(_oTranslateValue != null ? ((Framed8VectorTrack) _oTranslateValue).unpack() : null);
        break;
      default: break;
    }
    _o.setTranslate(_oTranslate);
  }
  public static int pack(FlatBufferBuilder builder, BoneTrackT _o) {
    if (_o == null) return 0;
    int _name = _o.getName() == null ? 0 : builder.createString(_o.getName());
    byte _scaleType = _o.getScale() == null ? VectorTrack.NONE : _o.getScale().getType();
    int _scale = _o.getScale() == null ? 0 : VectorTrackUnion.pack(builder, _o.getScale());
    byte _rotateType = _o.getRotate() == null ? RotationTrack.NONE : _o.getRotate().getType();
    int _rotate = _o.getRotate() == null ? 0 : RotationTrackUnion.pack(builder, _o.getRotate());
    byte _translateType = _o.getTranslate() == null ? VectorTrack.NONE : _o.getTranslate().getType();
    int _translate = _o.getTranslate() == null ? 0 : VectorTrackUnion.pack(builder, _o.getTranslate());
    return createBoneTrack(
      builder,
      _name,
      _scaleType,
      _scale,
      _rotateType,
      _rotate,
      _translateType,
      _translate);
  }
}

