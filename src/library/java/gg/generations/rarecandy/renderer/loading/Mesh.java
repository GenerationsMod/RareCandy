package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

public record Mesh(
        String name,
        int material,
        List<Integer> indices,
        List<Vector3f> positions,
        List<Vector2f> uvs,
        List<Vector3f> normals,
        List<Bone> bones
) {

    public static Mesh[] readMeshData(Skeleton skeleton, AIScene scene) {
        var meshes = new Mesh[scene.mNumMeshes()];

        for (int i = 0; i < scene.mNumMeshes(); i++) {

            var mesh = AIMesh.create(scene.mMeshes().get(i));
            var name = mesh.mName().dataString();
            var material = mesh.mMaterialIndex();
            var indices = new ArrayList<Integer>();
            var positions = new ArrayList<Vector3f>();
            var uvs = new ArrayList<Vector2f>();
            var normals = new ArrayList<Vector3f>();
            var bones = new ArrayList<Bone>();

            // Indices
            var aiFaces = mesh.mFaces();

            for (int j = 0; j < mesh.mNumFaces(); j++) {
                var aiFace = aiFaces.get(j);
                indices.add(aiFace.mIndices().get(0));
                indices.add(aiFace.mIndices().get(1));
                indices.add(aiFace.mIndices().get(2));
            }

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
            meshes[i] = new Mesh(name, material, indices, positions, uvs, normals, bones);
        }

        skeleton.calculateBoneData();
        return meshes;
    }
}
