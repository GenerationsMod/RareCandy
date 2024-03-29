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

                return;
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(ids) + " " + Arrays.toString(weights);
    }

    public boolean isEmpty() {
        return ids[0] == 0 && ids[1] == 0 && ids[2] == 0 && ids[3] == 0 && weights[0] == 0 && weights[1] == 0 && weights[2] == 0 && weights[3] == 0;
    }
}