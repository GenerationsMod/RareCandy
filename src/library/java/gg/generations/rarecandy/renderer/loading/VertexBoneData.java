package gg.generations.rarecandy.renderer.loading;

import java.util.Arrays;

public record VertexBoneData(int[] ids, float[] weights) {
    public VertexBoneData() {
        this(new int[4], new float[4]);
    }

    public void addBoneData(int boneId, float weight) {
        for (var i = 0 ; i < ids.length; i++) {
            if (weights[i] == 0.0) {
                ids[i] = boneId;
                weights[i] = weight;

//                if(weight != 1.0) System.out.println("%s %s".formatted(boneId, weight));
                return;
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(ids) + " " + Arrays.toString(weights);
    }
}