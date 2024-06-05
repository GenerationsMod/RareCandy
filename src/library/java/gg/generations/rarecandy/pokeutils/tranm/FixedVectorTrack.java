package gg.generations.rarecandy.pokeutils.tranm;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class FixedVectorTrack extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static FixedVectorTrack getRootAsFixedVectorTrack(ByteBuffer _bb) {
        return getRootAsFixedVectorTrack(_bb, new FixedVectorTrack());
    }

    public static FixedVectorTrack getRootAsFixedVectorTrack(ByteBuffer _bb, FixedVectorTrack obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public FixedVectorTrack __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public Vec3 co() {
        return co(new Vec3());
    }

    public Vec3 co(Vec3 obj) {
        int o = __offset(4);
        return o != 0 ? obj.__assign(o + bb_pos, bb) : null;
    }

    public static void startFixedVectorTrack(FlatBufferBuilder builder) {
        builder.startTable(1);
    }

    public static void addCo(FlatBufferBuilder builder, int coOffset) {
        builder.addStruct(0, coOffset, 0);
    }

    public static int endFixedVectorTrack(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public FixedVectorTrack get(int j) {
            return get(new FixedVectorTrack(), j);
        }

        public FixedVectorTrack get(FixedVectorTrack obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }

    public FixedVectorTrackT unpack() {
        FixedVectorTrackT _o = new FixedVectorTrackT();
        unpackTo(_o);
        return _o;
    }

    public void unpackTo(FixedVectorTrackT _o) {
        if (co() != null) co().unpackTo(_o.getCo());
        else _o.setCo(null);
    }

    public static int pack(FlatBufferBuilder builder, FixedVectorTrackT _o) {
        if (_o == null) return 0;
        startFixedVectorTrack(builder);
        addCo(builder, Vec3.pack(builder, _o.getCo()));
        return endFixedVectorTrack(builder);
    }
}

