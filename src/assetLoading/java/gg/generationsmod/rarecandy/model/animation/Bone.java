package gg.generationsmod.rarecandy.model.animation;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIBone;

import java.util.Objects;

/**
 * We re-use this structure for multiple meshes so if you are accessing this value from outside a meshes bone array do NOT trust it.
 */
public class Bone {

    public String name;
    public VertexWeight[] weights;
    public Matrix4f inverseBindMatrix;

    @Override
    public String toString() {
        return "Bone{" + "name='" + name + '\'' + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static Bone from(AIBone bone) {
        var b = new Bone();
        b.inverseBindMatrix = BoneNode.from(bone.mOffsetMatrix());
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
