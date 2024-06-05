package gg.generations.rarecandy.renderer.animation;

import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class TranmUtilExperimental {
    public static final float PI_DIVISOR = (float) (Math.PI / 65536);
    public static final float PI_ADDEND = (float) (Math.PI / 4.0);


    public static float expand_float(int i) {
        return i * PI_DIVISOR - PI_ADDEND;
    }

    public static int quantize_float(float i) {
        var result = (int) ((i + PI_ADDEND) / PI_DIVISOR);
        return result & 0x7FFF;
    }

    public static int[] pack(Quaternionf q) {
        return pack1(Stream.of(q.x(), q.y(), q.z(), q.w()).collect(Collectors.toCollection(ArrayList::new)));
    }

    public static int[] pack1(List<Float> q) {
        q = new ArrayList<>(q);
        var max_val = max(q);
        var min_val = min(q);
        var is_negative = 0;

        if (abs(min_val) > max_val) {
            max_val = min_val;
            is_negative = 1;
        }

        var max_index = q.indexOf(max_val);
        int tx = 0, ty = 0, tz = 0;

        if (is_negative == 1)
            q = Stream.of(-q.get(0), -q.get(1), -q.get(2), -q.get(3)).collect(Collectors.toCollection(ArrayList::new));

        if (max_index == 0) {
            tx = quantize_float(q.get(3));
            ty = quantize_float(q.get(1));
            tz = quantize_float(q.get(2));
        } else if (max_index == 1) {
            tx = quantize_float(q.get(0));
            ty = quantize_float(q.get(3));
            tz = quantize_float(q.get(2));
        } else if (max_index == 2) {
            tx = quantize_float(q.get(0));
            ty = quantize_float(q.get(1));
            tz = quantize_float(q.get(3));
        } else if (max_index == 3) {
            tx = quantize_float(q.get(0));
            ty = quantize_float(q.get(1));
            tz = quantize_float(q.get(2));
        }

        long pack = (((long) tz << 30) | ((long) ty << 15) | tx);
        pack = (pack << 3) | ((is_negative << 2) | max_index);
//        System.out.println(pack & 0xFFFF + ", " + (pack >> 16) & 0xFFFF + ", " + (pack >> 32) & 0xFFFF));
        return new int[]{(int) (pack & 0xFFFF), (int) ((pack >> 16) & 0xFFFF), (int) ((pack >> 32) & 0xFFFF)};
    }

    public static Quaternionf unpack(int X, int Y, int Z) {
        long pack = ((long) Z << 32) | ((long) Y << 16) | X;
        var flags = pack & 7;
        var missing_component = flags & 3;
        var isNegative = (flags >> 2) != 0;

        pack >>= 3;
        var tx = expand_float((int) ((pack) & 0x7FFF));
        var ty = expand_float((int) ((pack >> 15) & 0x7FFF));
        var tz = expand_float((int) ((pack >> 30) & 0x7FFF));
        var tw = (float) Math.sqrt(Math.max(1.0f - (tx * tx + ty * ty + tz * tz), 0.0f));
//            print(X, Y, Z)
//    #quat order is x, y, z, w
//    #print(f"original comp: {missing_component}")

        Quaternionf result = new Quaternionf();
        if (missing_component == 0) result.set(tw, ty, tz, tx);
        else if (missing_component == 1) result.set(tx, tw, tz, ty);
        else if (missing_component == 2) result.set(tx, ty, tw, tz);
        else result.set(tx, ty, tz, tw);

        if (isNegative) result.mul(-1);
        ;

        return result;
    }
}
