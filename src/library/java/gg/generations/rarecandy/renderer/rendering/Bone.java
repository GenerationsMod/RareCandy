package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.pokeutils.ModelNode;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIBone;

import java.util.Objects;

/**
 * We re-use this structure for multiple meshes so if you are accessing this value from outside a meshes bone array do NOT trust it.
 */
public class Bone {

    public String name;
    public VertexWeight[] weights;

    public Matrix4f inverseBindMatrix;
    public Matrix4f restPose;

    public final Vector3f posePosition = new Vector3f();
    public final Quaternionf poseRotation = new Quaternionf();
    public final Vector3f poseScale = new Vector3f(1, 1, 1);

    public Matrix4f lastSuccessfulTransform = new Matrix4f().identity();

    @Override
    public String toString() {
        return "Bone{" + "name='" + name + '\'' + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bone bone)) return false;

        return name.equals(bone.name);
    }

    public static Bone from(AIBone bone) {
        var b = new Bone();
        b.inverseBindMatrix = ModelNode.from(bone.mOffsetMatrix());
        b.restPose = new Matrix4f().set(b.inverseBindMatrix).invert();
        b.name = bone.mName().dataString();

        var aiWeights = Objects.requireNonNull(bone.mWeights());
        var vertexWeights = new Bone.VertexWeight[aiWeights.capacity()];
        for (int i = 0; i < aiWeights.capacity(); i++) {
            var aiWeight = aiWeights.get(i);
            vertexWeights[i] = new Bone.VertexWeight(aiWeight.mVertexId(), aiWeight.mWeight());
        }

        b.weights = vertexWeights;
        return b;
    }

    public static class VertexWeight {

        public int vertexId;
        public float weight;

        public VertexWeight(int vertexId, float weight) {
            this.vertexId = vertexId;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "(" + vertexId +
                    ", " + weight +
                    ")";
        }
    }
}
