package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

public class Bone {
    public String name;
    public VertexWeight[] weights;
    public Matrix4f offsetMatrix;

    @Override
    public String toString() {
        return name;
    }

    public static class VertexWeight {

        public int vertexId;
        public float weight;

        public VertexWeight(int vertexId, float weight) {
            this.vertexId = vertexId;
            this.weight = weight;
        }
    }
}
