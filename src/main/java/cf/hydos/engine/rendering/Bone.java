package cf.hydos.engine.rendering;

import org.joml.Matrix4f;

public class Bone {

    public String name;
    public VertexWeight[] weights;
    public Matrix4f offsetMatrix;

    public Matrix4f finalTransformation; // TODO: figure out what to do with this

    public static class VertexWeight {

        public int vertexId;
        public float weight;

        public VertexWeight(int vertexId, float weight) {
            this.vertexId = vertexId;
            this.weight = weight;
        }
    }
}
