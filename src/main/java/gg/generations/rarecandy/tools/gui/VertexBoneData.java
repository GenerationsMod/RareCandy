package gg.generations.rarecandy.tools.gui;

import java.util.stream.IntStream;

public record VertexBoneData(int[] ids, float[] weights) {
        public VertexBoneData() {
            this(new int[4], new float[4]);
        }

        public void addBoneData(int BoneID, float Weight) {
            for (var i = 0 ; i < ids.length; i++) {
                if (weights[i] == 0.0) {
                    ids[i] = BoneID;
                    weights[i] = Weight;
                    return;
                }
            }

            // should never get here - more bones than we have space for
        }
    }