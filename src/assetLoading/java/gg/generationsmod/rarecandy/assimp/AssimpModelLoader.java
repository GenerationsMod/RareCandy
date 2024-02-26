package gg.generationsmod.rarecandy.assimp;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import gg.generationsmod.rarecandy.FileLocator;
import gg.generationsmod.rarecandy.model.Mesh;
import gg.generationsmod.rarecandy.model.RawModel;
import gg.generationsmod.rarecandy.model.animation.*;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;
import org.apache.commons.compress.utils.FileNameUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;
import org.msgpack.core.MessagePack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static gg.generationsmod.rarecandy.model.config.pk.ModelConfig.*;
import static java.util.Objects.requireNonNull;

public class AssimpModelLoader {
    private static BiConsumer<String, BufferedImage> consumer;

    public static void setImageConsumer(BiConsumer<String, BufferedImage> consumer) {

        AssimpModelLoader.consumer = consumer;
    }

    public static RawModel load(String name, FileLocator locator, int extraFlags) {
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

        var scene = Assimp.aiImportFileEx(name, Assimp.aiProcess_Triangulate | Assimp.aiProcess_ImproveCacheLocality | extraFlags, fileIo);
        if (scene == null) throw new RuntimeException(Assimp.aiGetErrorString());
        var result = readScene(scene, locator);
        Assimp.aiReleaseImport(scene);
        return result;
    }

    private static RawModel readScene(AIScene scene, FileLocator locator) {
        var skeleton = new Skeleton(BoneNode.create(scene.mRootNode()));
        var config = readConfig(locator);
        var materials = readMaterialData(scene);
        var images = readImages(locator);
        var meshes = readMeshData(skeleton, scene, new HashMap<>());
        var animations = readAnimations(locator, skeleton, config);
        return new RawModel(materials, meshes, skeleton, config, images, animations);
    }

    private static Map<String, Animation<?>> readAnimations(FileLocator locator, Skeleton skeleton, ModelConfig config) {
        var map = new HashMap<String, Animation<?>>();

        for(var file : locator.getFiles()) {
            var base = FileNameUtils.getBaseName(file);
            var ext = FileNameUtils.getExtension(file);


            try {
                Animation<?> anim = switch (FileNameUtils.getExtension(file)) {
                    case "tranm" -> new TranmAnimation(base, locator.getFile(file), locator.getFile(base + ".tracm"), skeleton);
//                    case "tracm" -> new TranmAnimation(base, locator.getFile(file), locator.getFile(base + ".tranm"), skeleton);
                    case "gfbanm" -> new GfbAnimation(base, locator.getFile(file), skeleton, config);
                    case "smd" -> new SmdAnimation(base, new SMDBinaryReader().read(MessagePack.newDefaultUnpacker(locator.getFile(file))), skeleton, config.animationFpsOverride.getOrDefault(base, 30));
                    case "smdx" -> new SmdAnimation(base, new SMDTextReader().read(new String(locator.getFile(file))), skeleton, config.animationFpsOverride.getOrDefault(base, 30));
                    default -> null;
                };
                if(anim != null) map.put(base, anim);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to load " + file);
            }
        }

        return map;
    }

    private static Map<String, String> readImages(FileLocator locator) {
        var images = locator.getFiles().stream().filter(key -> key.endsWith("jxl") || key.endsWith("jpg") || key.endsWith("png")).collect(Collectors.toMap(a -> a, locator::readImage));
        var name = locator.getPath().getFileName().toString();

        var map = new HashMap<String, String>();
        for (var entry : images.entrySet()) {
            var key = entry.getKey();

            var id = name + "-" + key;
            consumer.accept(id, entry.getValue());
            map.put(key, id);
        }

        return map;
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
