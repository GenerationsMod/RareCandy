package gg.generations.rarecandy.animation;

import gg.generations.pokeutils.tranm.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class TranmUtil {
    private static final int[][] QUATERNION_SWIZZLES = {
            new int[]{0, 3, 2, 1}, new int[]{3, 0, 2, 1},
            new int[]{3, 2, 0, 1}, new int[]{3, 2, 1, 0}
    };

    private static short unpackS15(short u15) {
        int sign = (u15 >> 14) & 1;
        u15 &= 0x3FFF;
        if (sign == 0) u15 -= 0x4000;
        return u15;
    }

    public static Vector3i quatToPacked(Quaternionf q) {
/*        var result = new Vector3i();

        int count = 15;
        int BASE = (1 << count) - 1;
        var maxVal = 1 / (0x399E * (float) Math.sqrt(2.0)); // such obvious, so constant, wow

        long cq = x & 0xFFFF;
        cq <<= 16;
        cq |= (int) y & 0xFFFF;
        cq <<= 16;
        cq |= (int) z & 0xFFFF;

        short extra = (short) (cq & 0x7);
        long num = cq >> 3;

        var x = unpackS15((short) ((num >> (count * 2)) & BASE));
        var y = unpackS15((short) ((num >> (count * 1)) & BASE));
        var z = unpackS15((short) ((num >> (count * 0)) & BASE));

        float fx = x * maxVal;
        float fy = y * maxVal;
        float fz = z * maxVal;

        float[] quat = {
                (float) Math.sqrt(1 - fx * fx - fy * fy - fz * fz),
                fx,
                fy,
                fz
        };

        int[] qmap = QUATERNION_SWIZZLES[(extra & 3)];
        Quaternionf q = new Quaternionf(quat[qmap[0]], quat[qmap[1]], quat[qmap[2]], quat[qmap[3]]);
        if ((extra >> 2) != 0) q.mul(-1);

        return result;*/
        return null;
    }

    public static Quaternionf packedToQuat(short z, short y, short x) {
        int count = 15;
        int BASE = (1 << count) - 1;
        float maxVal = 1 / (0x399E * (float) Math.sqrt(2.0)); // such obvious, so constant, wow

        long cq = x & 0xFFFF;
        cq <<= 16;
        cq |= (int) y & 0xFFFF;
        cq <<= 16;
        cq |= (int) z & 0xFFFF;

        short extra = (short) (cq & 0x7);
        long num = cq >> 3;

        x = unpackS15((short) ((num >> (count * 2)) & BASE));
        y = unpackS15((short) ((num >> (count)) & BASE));
        z = unpackS15((short) ((num >> (0)) & BASE));

        float fx = x * maxVal;
        float fy = y * maxVal;
        float fz = z * maxVal;

        float[] quat = {
                (float) Math.sqrt(1 - fx * fx - fy * fy - fz * fz),
                fx,
                fy,
                fz
        };

        int[] qmap = QUATERNION_SWIZZLES[(extra & 3)];
        Quaternionf q = new Quaternionf(quat[qmap[0]], quat[qmap[1]], quat[qmap[2]], quat[qmap[3]]);
        if ((extra >> 2) != 0) q.mul(-1);

        return q;
    }

    public static void processDynamicQuatTrack(DynamicQuatTrack track, TransformStorage<Quaternionf> rotationKeys) {
        for (int i = 0; i < track.vecLength(); i++) {
            var vec = track.vec(i);
            rotationKeys.add(i, TranmUtil.packedToQuat((short) vec.x(), (short) vec.y(), (short) vec.z()));
        }
    }

    public static void processFixedQuatTrack(FixedQuatTrack track, TransformStorage<Quaternionf> rotationKeys) {
        var vec = track.vec();
        rotationKeys.add(0, TranmUtil.packedToQuat((short) vec.x(), (short) vec.y(), (short) vec.z()));
    }

    public static void processFramed8QuatTrack(Framed8QuatTrack track, TransformStorage<Quaternionf> rotationKeys) {
        var frames = track.framesVector();
        for (int i = 0; i < track.vecLength(); i++) {
            int frame = i;
            var vec = track.vec(i);

            if (i < frames.length()) frame = frames.getAsUnsigned(i);
            rotationKeys.add(frame, TranmUtil.packedToQuat((short) vec.x(), (short) vec.y(), (short) vec.z()));
        }
    }

    public static void processFramed16QuatTrack(Framed16QuatTrack track, TransformStorage<Quaternionf> rotationKeys) {
        var frames = track.framesVector();
        for (int i = 0; i < track.vecLength(); i++) {
            int frame = i;
            var vec = track.vec(i);

            if (i < frames.length()) frame = frames.getAsUnsigned(i);
            rotationKeys.add(frame, TranmUtil.packedToQuat((short) vec.x(), (short) vec.y(), (short) vec.z()));
        }
    }

    public static void processDynamicVecTrack(DynamicVectorTrack track, TransformStorage<Vector3f> vecKeys) {
        for (int i = 0; i < track.vecLength(); i++) {
            var vec = track.vec(i);
            vecKeys.add(i, new Vector3f(vec.x(), vec.y(), vec.z()));
        }
    }

    public static void processFixedVecTrack(FixedVectorTrack track, TransformStorage<Vector3f> vecKeys) {
        var vec = track.vec();
        vecKeys.add(0, new Vector3f(vec.x(), vec.y(), vec.z()));
    }

    public static void processFramed8VecTrack(Framed8VectorTrack track, TransformStorage<Vector3f> vecKeys) {
        for (int i = 0; i < track.vecLength(); i++) {
            int frame = i;
            var vec = track.vec(i);

            if (i < track.framesLength()) frame = track.frames(i);
            vecKeys.add(frame, new Vector3f(vec.x(), vec.y(), vec.z()));
        }
    }

    public static void processFramed16VecTrack(Framed16VectorTrack track, TransformStorage<Vector3f> vecKeys) {
        for (int i = 0; i < track.vecLength(); i++) {
            int frame = i;
            var vec = track.vec(i);

            if (i < track.framesLength()) frame = track.frames(i);
            vecKeys.add(frame, new Vector3f(vec.x(), vec.y(), vec.z()));
        }
    }
}
