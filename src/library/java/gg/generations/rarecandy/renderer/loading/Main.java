package gg.generations.rarecandy.renderer;

import jdk.incubator.vector.FloatVector;

import java.util.Arrays;

import static com.traneptora.jxlatte.util.MathHelper.SPECIES;

public class Main {
    public static void main(String[] args) {
        var v1 = FloatVector.fromArray(SPECIES, new float []{1, 2, 3, 4, 5, 6}, 0);
        var v2 = FloatVector.fromArray(SPECIES, new float []{7, 8, 9, 10, 11, 12}, 0);
        var v3 = FloatVector.fromArray(SPECIES, new float []{13,14,15,16,17,18}, 0);


        var dest = new float[6* 3];
        v1.intoArray(dest, 1);

        System.out.println(Arrays.toString(dest));
    }
}
