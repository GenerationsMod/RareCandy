package gg.generations.rarecandy.pokeutils.tranm;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class Vec4 extends Struct {
    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Vec4 __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public float x() {
        return bb.getFloat(bb_pos + 0);
    }

    public float y() {
        return bb.getFloat(bb_pos + 4);
    }

    public float z() {
        return bb.getFloat(bb_pos + 8);
    }

    public float w() {
        return bb.getFloat(bb_pos + 12);
    }

    public static int createVec4(FlatBufferBuilder builder, float x, float y, float z, float w) {
        builder.prep(4, 16);
        builder.putFloat(w);
        builder.putFloat(z);
        builder.putFloat(y);
        builder.putFloat(x);
        return builder.offset();
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public Vec4 get(int j) {
            return get(new Vec4(), j);
        }

        public Vec4 get(Vec4 obj, int j) {
            return obj.__assign(__element(j), bb);
        }
    }

    public Vec4T unpack() {
        Vec4T _o = new Vec4T();
        unpackTo(_o);
        return _o;
    }

    public void unpackTo(Vec4T _o) {
        float _oX = x();
        _o.setX(_oX);
        float _oY = y();
        _o.setY(_oY);
        float _oZ = z();
        _o.setZ(_oZ);
        float _oW = w();
        _o.setW(_oW);
    }

    public static int pack(FlatBufferBuilder builder, Vec4T _o) {
        if (_o == null) return 0;
        return createVec4(
                builder,
                _o.getX(),
                _o.getY(),
                _o.getZ(),
                _o.getW());
    }
}

