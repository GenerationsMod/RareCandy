package com.pixelmongenerations.pixelmonassetutils.scene.objects;

import com.pixelmongenerations.inception.rendering.Bone;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Mesh implements SceneObject {

    private final String name;
    private final Vector3f[] vertices;
    private final int[] indices;
    private final Vector3f[] normals;
    private final Vector2f[] texCoords;
    private final Material material;
    @Nullable
    private final Vector3f[] tangents;
    @Nullable
    private final Bone[] skeleton;

    public Mesh(String name, Vector3f[] vertices, int[] indices, Vector3f[] normals, Vector2f[] texCoords, @Nullable Vector3f[] tangents, @Nullable Bone[] skeleton, Material material) {
        this.name = name;
        this.vertices = vertices;
        this.indices = indices;
        this.normals = normals;
        this.texCoords = texCoords;
        this.tangents = tangents;
        this.skeleton = skeleton;
        this.material = material;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Vector3f[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public Vector3f[] getNormals() {
        return normals;
    }

    public Vector3f[] getTangents() {
        return tangents;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public Material getMaterial() {
        return material;
    }

    @Nullable
    public Bone[] getBones() {
        return skeleton;
    }
}
