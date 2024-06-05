// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.GFLib.Anim;

import com.google.flatbuffers.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Framed16VectorTrack extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static Framed16VectorTrack getRootAsFramed16VectorTrack(ByteBuffer _bb) {
        return getRootAsFramed16VectorTrack(_bb, new Framed16VectorTrack());
    }

    public static Framed16VectorTrack getRootAsFramed16VectorTrack(ByteBuffer _bb, Framed16VectorTrack obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Framed16VectorTrack __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int frames(int j) {
        int o = __offset(4);
        return o != 0 ? bb.getShort(__vector(o) + j * 2) & 0xFFFF : 0;
    }

    public int framesLength() {
        int o = __offset(4);
        return o != 0 ? __vector_len(o) : 0;
    }

    public ShortVector framesVector() {
        return framesVector(new ShortVector());
    }

    public ShortVector framesVector(ShortVector obj) {
        int o = __offset(4);
        return o != 0 ? obj.__assign(__vector(o), bb) : null;
    }

    public ByteBuffer framesAsByteBuffer() {
        return __vector_as_bytebuffer(4, 2);
    }

    public ByteBuffer framesInByteBuffer(ByteBuffer _bb) {
        return __vector_in_bytebuffer(_bb, 4, 2);
    }

    public Vec3 co(int j) {
        return co(new Vec3(), j);
    }

    public Vec3 co(Vec3 obj, int j) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__vector(o) + j * 12, bb) : null;
    }

    public int coLength() {
        int o = __offset(6);
        return o != 0 ? __vector_len(o) : 0;
    }

    public Vec3.Vector coVector() {
        return coVector(new Vec3.Vector());
    }

    public Vec3.Vector coVector(Vec3.Vector obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__vector(o), 12, bb) : null;
    }

    public static int createFramed16VectorTrack(FlatBufferBuilder builder,
                                                int framesOffset,
                                                int coOffset) {
        builder.startTable(2);
        Framed16VectorTrack.addCo(builder, coOffset);
        Framed16VectorTrack.addFrames(builder, framesOffset);
        return Framed16VectorTrack.endFramed16VectorTrack(builder);
    }

    public static void startFramed16VectorTrack(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addFrames(FlatBufferBuilder builder, int framesOffset) {
        builder.addOffset(0, framesOffset, 0);
    }

    public static int createFramesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(2, data.length, 2);
        for (int i = data.length - 1; i >= 0; i--) builder.addShort((short) data[i]);
        return builder.endVector();
    }

    public static void startFramesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(2, numElems, 2);
    }

    public static void addCo(FlatBufferBuilder builder, int coOffset) {
        builder.addOffset(1, coOffset, 0);
    }

    public static void startCoVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(12, numElems, 4);
    }

    public static int endFramed16VectorTrack(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public Framed16VectorTrack get(int j) {
            return get(new Framed16VectorTrack(), j);
        }

        public Framed16VectorTrack get(Framed16VectorTrack obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }

    public Framed16VectorTrackT unpack() {
        Framed16VectorTrackT _o = new Framed16VectorTrackT();
        unpackTo(_o);
        return _o;
    }

    public void unpackTo(Framed16VectorTrackT _o) {
        int[] _oFrames = new int[framesLength()];
        for (int _j = 0; _j < framesLength(); ++_j) {
            _oFrames[_j] = frames(_j);
        }
        _o.setFrames(_oFrames);
        Vec3T[] _oCo = new Vec3T[coLength()];
        for (int _j = 0; _j < coLength(); ++_j) {
            _oCo[_j] = (co(_j) != null ? co(_j).unpack() : null);
        }
        _o.setCo(_oCo);
    }

    public static int pack(FlatBufferBuilder builder, Framed16VectorTrackT _o) {
        if (_o == null) return 0;
        int _frames = 0;
        if (_o.getFrames() != null) {
            _frames = createFramesVector(builder, _o.getFrames());
        }
        int _co = 0;
        Vec3T[] _oCo = _o.getCo();
        if (_oCo != null) {
            int _unused_offset = 0;
            startCoVector(builder, _oCo.length);
            for (int _j = _oCo.length - 1; _j >= 0; _j--) {
                _unused_offset = Vec3.pack(builder, _oCo[_j]);
            }
            _co = builder.endVector();
        }
        return createFramed16VectorTrack(
                builder,
                _frames,
                _co);
    }
}

