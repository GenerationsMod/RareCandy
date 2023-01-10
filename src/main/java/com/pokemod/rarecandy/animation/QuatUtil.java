package com.pokemod.rarecandy.animation;

import com.pokemod.miraidon.Vec3s;
import org.joml.Quaternionf;

public class QuatUtil {
    private static short UnpackS15(short u15)
    {
        int sign = (u15 >> 14) & 1;
        u15 &= 0x3FFF;
        if (sign == 0) u15 -= 0x4000;
        return u15;
    }

    static int[][] QUATERNION_SWIZZLES =  {
            new int[] { 0, 3, 2, 1 }, new int[] { 3, 0, 2, 1 },
            new int[] { 3, 2, 0, 1 }, new int[] { 3, 2, 1, 0 }
    };

    public static Quaternionf packedToQuat(Vec3s vec) {
        int x = vec.x();
        int y = vec.y();
        int z = vec.z();

        int count = 15;
        int BASE = (1 << count) - 1;
        float maxval = 1 / (0x399E * (float) Math.sqrt(2.0)); // such obvious, so constant, wow

        long cq = x & 0xFFFF;
        cq <<= 16;
        cq |= (int) y & 0xFFFF;
        cq <<= 16;
        cq |= (int) z & 0xFFFF;


        short extra = (short)(cq & 0x7);

        long num = cq >> 3;

        x = UnpackS15((short)((num >> (count * 2)) & BASE));
        y = UnpackS15((short)((num >> (count * 1)) & BASE));
        z = UnpackS15((short)((num >> (count * 0)) & BASE));

        float fx = x * maxval;
        float fy = y * maxval;
        float fz = z * maxval;

        float[] quat = {
                (float)Math.sqrt(1 - fx * fx - fy * fy - fz * fz),
                fx,
                fy,
                fz };

        int[] qmap = QUATERNION_SWIZZLES[(extra & 3)];
        Quaternionf q = new Quaternionf(quat[qmap[0]], quat[qmap[1]], quat[qmap[2]], quat[qmap[3]]);
        if ((extra >> 2) != 0) q.invert();

        return q;

    }
}
