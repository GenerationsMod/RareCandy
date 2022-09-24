package com.pixelmongenerations.rarecandy;

import com.pixelmongenerations.rarecandy.components.AnimatedRenderObject;
import com.pixelmongenerations.rarecandy.components.StaticRenderObject;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import com.pixelmongenerations.pixelmonassetutils.scene.Scene;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.pixelmonassetutils.scene.objects.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.Assimp;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Deprecated
public class OldModelLoader {

    @Deprecated
    public static AnimatedRenderObject loadAnimatedFile(Scene scene, AIScene aiScene) {
        if (aiScene.mNumAnimations() == 0) {
            System.err.println("the imported file does not contain any animations.");
            System.out.println(Assimp.aiGetErrorString());
            System.exit(0);
        }

        Mesh mesh = scene.meshes.get(0);

        int boneInfoOffset = 11; // 3 (Pos) + 2 (TexCoord) + 3 (Normal) + 3 (Tangent)
        int sizeOfVertex = boneInfoOffset + 4 + 4; // boneInfoOffset + 4 (BoneInfo1) + 4 (BoneInfo2)

        //=Vertex Data Format=
        // position data 3f
        // texcoord data 2f
        // normal   data 3f
        // tangent  data 3f
        //
        // bone		info 4f
        // bone		info 4f
        //====================
        float[] rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
        int index = 0;

        for (int v = 0; v < mesh.getVertices().length; v++) {
            Vector3f position = mesh.getVertices()[v];
            Vector3f normal = mesh.getNormals()[v];
            Vector3f tangent = mesh.getTangents()[v];
            Vector2f texCoord = mesh.getTexCoords()[v];

            rawMeshData[index++] = position.x();
            rawMeshData[index++] = position.y();
            rawMeshData[index++] = position.z();

            rawMeshData[index++] = texCoord.x();
            rawMeshData[index++] = texCoord.y();

            rawMeshData[index++] = normal.x();
            rawMeshData[index++] = normal.y();
            rawMeshData[index++] = normal.z();

            rawMeshData[index++] = tangent.x();
            rawMeshData[index++] = tangent.y();
            rawMeshData[index++] = tangent.z();

            rawMeshData[index++] = 0; // Bone ID Bone 1
            rawMeshData[index++] = 0; // Bone ID Bone 2
            rawMeshData[index++] = 0; // Weight  Bone 1
            rawMeshData[index++] = 0; // Weight  Bone 2

            rawMeshData[index++] = 0; // Bone ID Bone 3
            rawMeshData[index++] = 0; // Bone ID Bone 4
            rawMeshData[index++] = 0; // Weight  Bone 3
            rawMeshData[index++] = 0; // Weight  Bone 4
        }

        HashMap<Integer, Integer> bone_index_map0 = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map1 = new HashMap<>();

        for (int boneId = 0; boneId < mesh.getBones().length; boneId++) {
            Bone bone = Objects.requireNonNull(mesh.getBones()[boneId]);

            for (int weightId = 0; weightId < bone.weights.length; weightId++) {
                Bone.VertexWeight weight = bone.weights[weightId];
                int vertId = weight.vertexId;
                int pVertex = vertId * sizeOfVertex; // pointer to where a vertex starts in the array.

                if (!bone_index_map0.containsKey(vertId)) {
                    rawMeshData[(pVertex + boneInfoOffset)] = boneId;
                    rawMeshData[(pVertex + boneInfoOffset) + 2] = weight.weight;
                    bone_index_map0.put(vertId, 0);
                } else if (bone_index_map0.get(vertId) == 0) {
                    rawMeshData[(pVertex + boneInfoOffset) + 1] = boneId;
                    rawMeshData[(pVertex + boneInfoOffset) + 3] = weight.weight;
                    bone_index_map0.put(vertId, 1);
                } else if (!bone_index_map1.containsKey(vertId)) {
                    rawMeshData[(pVertex + boneInfoOffset) + 4] = boneId;
                    rawMeshData[(pVertex + boneInfoOffset) + 6] = weight.weight;
                    bone_index_map1.put(vertId, 0);
                } else if (bone_index_map1.get(vertId) == 0) {
                    rawMeshData[(pVertex + boneInfoOffset) + 5] = boneId;
                    rawMeshData[(pVertex + boneInfoOffset) + 7] = weight.weight;
                    bone_index_map1.put(vertId, 1);
                } else {
                    System.err.println("max 4 bones per vertex.");
                    System.exit(0);
                }
            }
        }

        Matrix4f inverseRootTransformation = scene.rootTransform;

        Bone[] bones = new Bone[mesh.getBones().length];
        for (int b = 0; b < mesh.getBones().length; b++) {
            bones[b] = mesh.getBones()[b];
        }

        AnimatedRenderObject object = new AnimatedRenderObject();
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);

        IntBuffer indices = BufferUtils.createIntBuffer(mesh.getIndices().length);
        for (int i : mesh.getIndices()) {
            indices.put(i);
        }
        indices.flip();

        for (float v : rawMeshData) vertBuffer.put(v);
        vertBuffer.flip();

        List<AITexture> rawTextures = new ArrayList<>();

        // Retrieve Textures
        PointerBuffer pTextures = aiScene.mTextures();
        if (pTextures != null) {
            for (int i = 0; i < aiScene.mNumTextures(); i++) {
                rawTextures.add(AITexture.create(pTextures.get(i)));
            }
        } else {
            throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
        }

        // Try to load the textures into rosella
        List<Texture> textures = new ArrayList<>();
        for (AITexture rawTexture : rawTextures) {
            if (rawTexture.mHeight() > 0) {
                throw new RuntimeException(".glb file had texture with height of 0");
            } else {
                textures.add(new Texture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
            }
        }

        object.upload(ShaderProgram.ANIMATED_SHADER, vertBuffer, indices, textures);
        object.animation = AIAnimation.create(Objects.requireNonNull(aiScene.mAnimations()).get(aiScene.mNumAnimations() - 1));
        object.bones = bones;
        object.boneTransforms = new Matrix4f[bones.length];
        object.root = aiScene.mRootNode();
        object.globalInverseTransform = inverseRootTransformation;
        return object;
    }

    /**
     * Uses old method of loading files. To be removed by 0.5.0
     */
    @Deprecated
    public static StaticRenderObject loadStaticFile(Scene scene, AIScene aiScene) {
        int sizeOfVertex = Float.BYTES * 3 + Float.BYTES * 2 + Float.BYTES * 3;

        for (Mesh mesh : scene.meshes) {
            float[] rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
            int index = 0;

            for (int v = 0; v < mesh.getVertices().length; v++) {
                Vector3f position = mesh.getVertices()[v];
                Vector3f normal = mesh.getNormals()[v];
                Vector2f texCoord = mesh.getTexCoords()[v];

                rawMeshData[index++] = position.x();
                rawMeshData[index++] = position.y();
                rawMeshData[index++] = position.z();

                rawMeshData[index++] = texCoord.x();
                rawMeshData[index++] = texCoord.y();

                rawMeshData[index++] = normal.x();
                rawMeshData[index++] = normal.y();
                rawMeshData[index++] = normal.z();
            }

            StaticRenderObject component = new StaticRenderObject();
            FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);

            IntBuffer indices = BufferUtils.createIntBuffer(mesh.getIndices().length);
            for (int i : mesh.getIndices()) {
                indices.put(i);
            }
            indices.flip();

            for (float v : rawMeshData) vertBuffer.put(v);
            vertBuffer.flip();

//            List<AITexture> rawTextures = new ArrayList<>();
//
//            // Retrieve Textures
//            PointerBuffer pTextures = aiScene.mTextures();
//            if (pTextures != null) {
//                for (int i = 0; i < aiScene.mNumTextures(); i++) {
//                    rawTextures.add(AITexture.create(pTextures.get(i)));
//                }
//            } else {
//                throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
//            }
//
//            // Try to load the textures into rosella
//            List<Texture> textures = new ArrayList<>();
//            for (AITexture rawTexture : rawTextures) {
//                if (rawTexture.mHeight() > 0) {
//                    throw new RuntimeException(".glb file had texture with height of 0");
//                } else {
//                    textures.add(new Texture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
//                }
//            }

            component.upload(ShaderProgram.STATIC_SHADER, vertBuffer, indices, scene.textures);
            return component;
        }
        throw new RuntimeException("Failed to create static object.");
    }
}
