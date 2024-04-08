package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public record Model(
        String[] names,
        int[] offsets,
        List<Vertex> vertices,
        List<Integer> indices
) {

    public record Vertex(Vector3f vector3f, Vector2f vector2f, Vector3f f, VertexBoneData vertexBoneData) {
    }


    public static Model readMeshData(Skeleton skeleton, AIScene scene) {
        var meshes = new Model[scene.mNumMeshes()];

        var names = new String[scene.mNumMeshes()];
        var offsets = new int[scene.mNumMeshes()];

        var vertices = new ArrayList<Vertex>();
        var indices = new ArrayList<Integer>();

        var index = 0;

        for (int i = 0; i < scene.mNumMeshes(); i++) {

            var mesh = AIMesh.create(scene.mMeshes().get(i));
            names[i] = mesh.mName().dataString();
            offsets[i] = index;
            var material = mesh.mMaterialIndex();

            var vertexMap = new ArrayList<Integer>();

            var positions = new ArrayList<Vector3f>();
            var uvs = new ArrayList<Vector2f>();
            var normals = new ArrayList<Vector3f>();
            var bones = new ArrayList<Bone>();

            // Positions
            var aiVert = mesh.mVertices();
            for (int j = 0; j < mesh.mNumVertices(); j++) {
                positions.add(new Vector3f(aiVert.get(j).x(), aiVert.get(j).y(), aiVert.get(j).z()));
            }


            // UV's
            var aiUV = mesh.mTextureCoords(0);
            if (aiUV != null) {
                while (aiUV.remaining() > 0) {
                    var uv = aiUV.get();
                    uvs.add(new Vector2f(uv.x(), 1 - uv.y()));
                }
            }

            // Normals
            var aiNormals = mesh.mNormals();
            if (aiNormals != null) {
                for (int j = 0; j < mesh.mNumVertices(); j++)
                    normals.add(new Vector3f(aiNormals.get(j).x(), aiNormals.get(j).y(), aiNormals.get(j).z()));
            }

            for (int j = 0; j < positions.size(); j++) {
                var vertex = new Vertex(positions.get(j), uvs.get(j), normals.get(j), new VertexBoneData());

                var vertexId = vertices.indexOf(vertex);

                if(vertexId == -1) {
                    vertexId = vertices.size();
                    vertices.add(vertex);
                }

                vertexMap.add(vertexId);
            }

            var aiFaces = mesh.mFaces();
            for (int j = 0; j < mesh.mNumFaces(); j++) {
                var aiFace = aiFaces.get(j);
                indices.add(vertexMap.get(aiFace.mIndices().get(0)));
                indices.add(vertexMap.get(aiFace.mIndices().get(1)));
                indices.add(vertexMap.get(aiFace.mIndices().get(2)));

            }


            // Bones
            if (mesh.mBones() != null) {
                var aiBones = requireNonNull(mesh.mBones());

                for (int j = 0; j < aiBones.capacity(); j++) {


                    var aiBone = AIBone.create(aiBones.get(j));
                    var bone = Bone.from(aiBone);


                    bones.add(bone);
                }
            }

            skeleton.store(bones.toArray(Bone[]::new));

            bones.forEach(bone -> {
                var boneId = skeleton.getId(bone);

                for(var weight : bone.weights) {
                    if(weight.weight == 0.0) return;
                    else {
                        vertices.get(vertexMap.get(weight.vertexId)).vertexBoneData().addBoneData(boneId, weight.weight);
                    }
                }
            });


//            meshes[i] = new Mesh(name, material, indices, positions, uvs, normals, bones);
        }

        skeleton.calculateBoneData();
        return new Model(names, offsets, vertices, indices);
    }
}
