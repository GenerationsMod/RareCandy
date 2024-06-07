package gg.generations.rarecandy.renderer.rendering;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

public class Bone {
    public final String name;
    public final Matrix4f inversePoseMatrix;
    public Matrix4f lastSuccessfulTransform = new Matrix4f().identity();

    public Bone(String name, Matrix4f inversePoseMatrix) {
        this.name = name;
        this.inversePoseMatrix = inversePoseMatrix;
    }

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
}
