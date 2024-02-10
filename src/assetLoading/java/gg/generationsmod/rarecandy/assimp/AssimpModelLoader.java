package gg.generationsmod.rarecandy.assimp;

import gg.generationsmod.rarecandy.FileLocator;
import gg.generationsmod.rarecandy.model.Mesh;
import gg.generationsmod.rarecandy.model.Model;
import gg.generationsmod.rarecandy.model.animation.BoneNode;
import gg.generationsmod.rarecandy.model.animation.Skeleton;
import gg.generationsmod.rarecandy.model.animation.Bone;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static gg.generationsmod.rarecandy.model.config.pk.ModelConfig.*;
import static java.util.Objects.requireNonNull;

public class AssimpModelLoader {

    public static Model load(String name, FileLocator locator, int extraFlags) {
        var fileIo = AIFileIO.create()
                .OpenProc((pFileIO, pFileName, openMode) -> {
                    var fileName = MemoryUtil.memUTF8(pFileName);
                    var bytes = locator.getFile(fileName);
                    var data = BufferUtils.createByteBuffer(bytes.length);
                    data.put(bytes);
                    data.flip();

                    return AIFile.create()
                            .ReadProc((pFile, pBuffer, size, count) -> {
                                var max = Math.min(data.remaining() / size, count);
                                MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, max * size);
                                data.position((int) (data.position() + max * size));
                                return max;
                            })
                            .SeekProc((pFile, offset, origin) -> {
                                switch (origin) {
                                    case Assimp.aiOrigin_CUR -> data.position(data.position() + (int) offset);
                                    case Assimp.aiOrigin_SET -> data.position((int) offset);
                                    case Assimp.aiOrigin_END -> data.position(data.limit() + (int) offset);
                                }

                                return 0;
                            })
                            .FileSizeProc(pFile -> data.limit())
                            .address();
                })
                .CloseProc((pFileIO, pFile) -> {
                    var aiFile = AIFile.create(pFile);
                    aiFile.ReadProc().free();
                    aiFile.SeekProc().free();
                    aiFile.FileSizeProc().free();
                });

        var scene = Assimp.aiImportFileEx(name, Assimp.aiProcess_Triangulate | Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_ImproveCacheLocality | extraFlags, fileIo);
        if (scene == null) throw new RuntimeException(Assimp.aiGetErrorString());
        var result = readScene(scene, locator);
        Assimp.aiReleaseImport(scene);
        return result;
    }

    private static Model readScene(AIScene scene, FileLocator locator) {
        var skeleton = new Skeleton(BoneNode.create(scene.mRootNode()));
        var config = readConfig(locator);
        var materials = readMaterialData(scene);
        var meshes = readMeshData(skeleton, scene, new HashMap<>());
        return new Model(materials, meshes, skeleton, config);
    }

    private static ModelConfig readConfig(FileLocator locator) {
        var json = new String(locator.getFile("config.json"));
        return GSON.fromJson(json, ModelConfig.class);
    }

    private static Mesh[] readMeshData(Skeleton skeleton, AIScene scene, Map<String, Bone> boneMap) {
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
            for (int j = 0; j < mesh.mNumVertices(); j++)
                positions.add(new Vector3f(aiVert.get(j).x(), aiVert.get(j).y(), aiVert.get(j).z()));

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
                    boneMap.put(bone.name, bone);
                }
            }

            skeleton.store(bones.toArray(Bone[]::new));
            meshes[i] = new Mesh(name, material, indices, positions, uvs, normals, bones);
        }

        skeleton.calculateBoneData();
        return meshes;
    }

    private static String[] readMaterialData(AIScene scene) {
        var materials = new String[scene.mNumMaterials()];

        for (int i = 0; i < scene.mNumMaterials(); i++) {
            var aiMat = AIMaterial.create(scene.mMaterials().get(i));

            for (int j = 0; j < aiMat.mNumProperties(); j++) {
                var property = AIMaterialProperty.create(aiMat.mProperties().get(j));
                var name = property.mKey().dataString();
                var data = property.mData();

                if (name.equals(Assimp.AI_MATKEY_NAME)) {
                    var matName = AIString.create(MemoryUtil.memAddress(data)).dataString();
                    materials[i] = matName;
                }
            }
        }

        return materials;
    }
}
