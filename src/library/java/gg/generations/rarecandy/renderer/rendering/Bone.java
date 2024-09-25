package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.assimp.AIBone;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * We re-use this structure for multiple meshes so if you are accessing this value from outside a meshes bone array do NOT trust it.
 */
public class Bone {

    public String name;

    public Matrix4f inverseBindMatrix;
    public Matrix4f restPose;

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

    public Bone(String name) {
        this.name = name;
        this.inverseBindMatrix = new Matrix4f();
        this.restPose = new Matrix4f();
    }
}
