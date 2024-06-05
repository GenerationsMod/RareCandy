package gg.generations.rarecandy.pokeutils.tracp;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Animation extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static Animation getRootAsAnimation(ByteBuffer _bb) {
        return getRootAsAnimation(_bb, new Animation());
    }

    public static Animation getRootAsAnimation(ByteBuffer _bb, Animation obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Animation __assign(int _i, ByteBuffer _bb) {
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

    public long res1() {
        int o = __offset(6);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public long unk2() {
        int o = __offset(8);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public long unk3() {
        int o = __offset(10);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public static int createAnimation(FlatBufferBuilder builder,
                                      int nameOffset,
                                      long res1,
                                      long unk2,
                                      long unk3) {
        builder.startTable(4);
        Animation.addUnk3(builder, unk3);
        Animation.addUnk2(builder, unk2);
        Animation.addRes1(builder, res1);
        Animation.addName(builder, nameOffset);
        return Animation.endAnimation(builder);
    }

    public static void startAnimation(FlatBufferBuilder builder) {
        builder.startTable(4);
    }

    public static void addName(FlatBufferBuilder builder, int nameOffset) {
        builder.addOffset(0, nameOffset, 0);
    }

    public static void addRes1(FlatBufferBuilder builder, long res1) {
        builder.addInt(1, (int) res1, (int) 0L);
    }

    public static void addUnk2(FlatBufferBuilder builder, long unk2) {
        builder.addInt(2, (int) unk2, (int) 0L);
    }

    public static void addUnk3(FlatBufferBuilder builder, long unk3) {
        builder.addInt(3, (int) unk3, (int) 0L);
    }

    public static int endAnimation(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public Animation get(int j) {
            return get(new Animation(), j);
        }

        public Animation get(Animation obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }

    public AnimationT unpack() {
        AnimationT _o = new AnimationT();
        unpackTo(_o);
        return _o;
    }

    public void unpackTo(AnimationT _o) {
        String _oName = name();
        _o.setName(_oName);
        long _oRes1 = res1();
        _o.setRes1(_oRes1);
        long _oUnk2 = unk2();
        _o.setUnk2(_oUnk2);
        long _oUnk3 = unk3();
        _o.setUnk3(_oUnk3);
    }

    public static int pack(FlatBufferBuilder builder, AnimationT _o) {
        if (_o == null) return 0;
        int _name = _o.getName() == null ? 0 : builder.createString(_o.getName());
        return createAnimation(
                builder,
                _name,
                _o.getRes1(),
                _o.getUnk2(),
                _o.getUnk3());
    }
}

