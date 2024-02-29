package gg.generationsmod.rarecandy.model;

import gg.generationsmod.rarecandy.model.animation.Bone;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.List;

public record Mesh(
        String name,
        int material,
        List<Integer> indices,
        List<Vector3f> positions,
        List<Vector2f> uvs,
        List<Vector3f> normals,
        List<Vector4i> boneIds,
        List<Vector4f> boneWeights
) {

}
