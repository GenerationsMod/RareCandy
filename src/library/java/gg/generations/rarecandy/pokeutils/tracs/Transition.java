package gg.generations.rarecandy.pokeutils.tracs;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Transition extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static Transition getRootAsTransition(ByteBuffer _bb) {
        return getRootAsTransition(_bb, new Transition());
    }

    public static Transition getRootAsTransition(ByteBuffer _bb, Transition obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Transition __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String path() {
        int o = __offset(4);
        return o != 0 ? __string(o + bb_pos) : null;
    }

    public ByteBuffer pathAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public ByteBuffer pathInByteBuffer(ByteBuffer _bb) {
        return __vector_in_bytebuffer(_bb, 4, 1);
    }

    public long hasExitTime() {
        int o = __offset(6);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public float exitTime() {
        int o = __offset(8);
        return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f;
    }

    public float duration() {
        int o = __offset(10);
        return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f;
    }

    public float offset() {
        int o = __offset(12);
        return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f;
    }

    public long canInterrupt() {
        int o = __offset(14);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public Condition conditions(int j) {
        return conditions(new Condition(), j);
    }

    public Condition conditions(Condition obj, int j) {
        int o = __offset(16);
        return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
    }

    public int conditionsLength() {
        int o = __offset(16);
        return o != 0 ? __vector_len(o) : 0;
    }

    public Condition.Vector conditionsVector() {
        return conditionsVector(new Condition.Vector());
    }

    public Condition.Vector conditionsVector(Condition.Vector obj) {
        int o = __offset(16);
        return o != 0 ? obj.__assign(__vector(o), 4, bb) : null;
    }

    public long type() {
        int o = __offset(18);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public static int createTransition(FlatBufferBuilder builder,
                                       int pathOffset,
                                       long hasExitTime,
                                       float exitTime,
                                       float duration,
                                       float offset,
                                       long canInterrupt,
                                       int conditionsOffset,
                                       long type) {
        builder.startTable(8);
        Transition.addType(builder, type);
        Transition.addConditions(builder, conditionsOffset);
        Transition.addCanInterrupt(builder, canInterrupt);
        Transition.addOffset(builder, offset);
        Transition.addDuration(builder, duration);
        Transition.addExitTime(builder, exitTime);
        Transition.addHasExitTime(builder, hasExitTime);
        Transition.addPath(builder, pathOffset);
        return Transition.endTransition(builder);
    }

    public static void startTransition(FlatBufferBuilder builder) {
        builder.startTable(8);
    }

    public static void addPath(FlatBufferBuilder builder, int pathOffset) {
        builder.addOffset(0, pathOffset, 0);
    }

    public static void addHasExitTime(FlatBufferBuilder builder, long hasExitTime) {
        builder.addInt(1, (int) hasExitTime, (int) 0L);
    }

    public static void addExitTime(FlatBufferBuilder builder, float exitTime) {
        builder.addFloat(2, exitTime, 0.0f);
    }

    public static void addDuration(FlatBufferBuilder builder, float duration) {
        builder.addFloat(3, duration, 0.0f);
    }

    public static void addOffset(FlatBufferBuilder builder, float offset) {
        builder.addFloat(4, offset, 0.0f);
    }

    public static void addCanInterrupt(FlatBufferBuilder builder, long canInterrupt) {
        builder.addInt(5, (int) canInterrupt, (int) 0L);
    }

    public static void addConditions(FlatBufferBuilder builder, int conditionsOffset) {
        builder.addOffset(6, conditionsOffset, 0);
    }

    public static int createConditionsVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]);
        return builder.endVector();
    }

    public static void startConditionsVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addType(FlatBufferBuilder builder, long type) {
        builder.addInt(7, (int) type, (int) 0L);
    }

    public static int endTransition(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public Transition get(int j) {
            return get(new Transition(), j);
        }

        public Transition get(Transition obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }

    public TransitionT unpack() {
        TransitionT _o = new TransitionT();
        unpackTo(_o);
        return _o;
    }

    public void unpackTo(TransitionT _o) {
        String _oPath = path();
        _o.setPath(_oPath);
        long _oHasExitTime = hasExitTime();
        _o.setHasExitTime(_oHasExitTime);
        float _oExitTime = exitTime();
        _o.setExitTime(_oExitTime);
        float _oDuration = duration();
        _o.setDuration(_oDuration);
        float _oOffset = offset();
        _o.setOffset(_oOffset);
        long _oCanInterrupt = canInterrupt();
        _o.setCanInterrupt(_oCanInterrupt);
        ConditionT[] _oConditions = new ConditionT[conditionsLength()];
        for (int _j = 0; _j < conditionsLength(); ++_j) {
            _oConditions[_j] = (conditions(_j) != null ? conditions(_j).unpack() : null);
        }
        _o.setConditions(_oConditions);
        long _oType = type();
        _o.setType(_oType);
    }

    public static int pack(FlatBufferBuilder builder, TransitionT _o) {
        if (_o == null) return 0;
        int _path = _o.getPath() == null ? 0 : builder.createString(_o.getPath());
        int _conditions = 0;
        if (_o.getConditions() != null) {
            int[] __conditions = new int[_o.getConditions().length];
            int _j = 0;
            for (ConditionT _e : _o.getConditions()) {
                __conditions[_j] = Condition.pack(builder, _e);
                _j++;
            }
            _conditions = createConditionsVector(builder, __conditions);
        }
        return createTransition(
                builder,
                _path,
                _o.getHasExitTime(),
                _o.getExitTime(),
                _o.getDuration(),
                _o.getOffset(),
                _o.getCanInterrupt(),
                _conditions,
                _o.getType());
    }
}

