// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.tracm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TrackMaterial extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static TrackMaterial getRootAsTrackMaterial(ByteBuffer _bb) {
        return getRootAsTrackMaterial(_bb, new TrackMaterial());
    }

    public static TrackMaterial getRootAsTrackMaterial(ByteBuffer _bb, TrackMaterial obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public TrackMaterial __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String name() {
        int o = __offset(4);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    public ByteBuffer nameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public ByteBuffer nameInByteBuffer(ByteBuffer _bb) {
        return __vector_in_bytebuffer(_bb, 4, 1);
    }

    public TrackMaterialInit initValues(int j) {
        return initValues(new TrackMaterialInit(), j);
    }

    public TrackMaterialInit initValues(TrackMaterialInit obj, int j) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    public int initValuesLength() {
        int o = __offset(6);
        return o != 0 ? __vector_len(o) : 0;
    }

    public TrackMaterialInit.Vector initValuesVector() {
        return initValuesVector(new TrackMaterialInit.Vector());
    }

    public TrackMaterialInit.Vector initValuesVector(TrackMaterialInit.Vector obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__vector(o), 4, bb) : null;
    }

    public TrackMaterialAnim animValues(int j) {
        return animValues(new TrackMaterialAnim(), j);
    }

    public TrackMaterialAnim animValues(TrackMaterialAnim obj, int j) {
        int o = __offset(8);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    public int animValuesLength() {
        int o = __offset(8);
        return o != 0 ? __vector_len(o) : 0;
    }

    public TrackMaterialAnim.Vector animValuesVector() {
        return animValuesVector(new TrackMaterialAnim.Vector());
    }

    public TrackMaterialAnim.Vector animValuesVector(TrackMaterialAnim.Vector obj) {
        int o = __offset(8);
        return o != 0 ? obj.__assign(__vector(o), 4, bb) : null;
    }

    public static int createTrackMaterial(FlatBufferBuilder builder,
                                          int nameOffset,
                                          int initValuesOffset,
                                          int animValuesOffset) {
        builder.startTable(3);
        TrackMaterial.addAnimValues(builder, animValuesOffset);
        TrackMaterial.addInitValues(builder, initValuesOffset);
        TrackMaterial.addName(builder, nameOffset);
        return TrackMaterial.endTrackMaterial(builder);
    }

    public static void startTrackMaterial(FlatBufferBuilder builder) {
        builder.startTable(3);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addInitValues(FlatBufferBuilder builder, int initValuesOffset) {
        builder.addOffset(1, initValuesOffset, 0);
    }

    public static int createInitValuesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]);
        return builder.endVector();
    }

    public static void startInitValuesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addAnimValues(FlatBufferBuilder builder, int animValuesOffset) {
        builder.addOffset(2, animValuesOffset, 0);
    }

    public static int createAnimValuesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]);
        return builder.endVector();
    }

    public static void startAnimValuesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endTrackMaterial(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public TrackMaterial get(int j) {
            return get(new TrackMaterial(), j);
        }

        public TrackMaterial get(TrackMaterial obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

