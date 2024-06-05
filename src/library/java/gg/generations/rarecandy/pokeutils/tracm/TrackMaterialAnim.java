// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.tracm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TrackMaterialAnim extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static TrackMaterialAnim getRootAsTrackMaterialAnim(ByteBuffer _bb) {
        return getRootAsTrackMaterialAnim(_bb, new TrackMaterialAnim());
    }

    public static TrackMaterialAnim getRootAsTrackMaterialAnim(ByteBuffer _bb, TrackMaterialAnim obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public TrackMaterialAnim __assign(int _i, ByteBuffer _bb) {
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

    public TrackMaterialChannels list() {
        return list(new TrackMaterialChannels());
    }

    public TrackMaterialChannels list(TrackMaterialChannels obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public static int createTrackMaterialAnim(FlatBufferBuilder builder,
                                              int nameOffset,
                                              int listOffset) {
        builder.startTable(2);
        TrackMaterialAnim.addList(builder, listOffset);
        TrackMaterialAnim.addName(builder, nameOffset);
        return TrackMaterialAnim.endTrackMaterialAnim(builder);
    }

    public static void startTrackMaterialAnim(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addList(FlatBufferBuilder builder, int listOffset) {
        builder.addOffset(1, listOffset, 0);
    }

    public static int endTrackMaterialAnim(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public TrackMaterialAnim get(int j) {
            return get(new TrackMaterialAnim(), j);
        }

        public TrackMaterialAnim get(TrackMaterialAnim obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

