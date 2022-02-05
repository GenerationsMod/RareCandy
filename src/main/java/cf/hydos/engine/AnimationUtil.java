package cf.hydos.engine;

import cf.hydos.engine.components.AnimatedRenderObject;
import cf.hydos.engine.rendering.Bone;
import cf.hydos.engine.rendering.shader.ShaderProgram;
import cf.hydos.pixelmonassetutils.scene.Scene;
import cf.hydos.pixelmonassetutils.scene.material.Texture;
import cf.hydos.pixelmonassetutils.scene.objects.Mesh;
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

public class AnimationUtil {
    public static AnimatedRenderObject loadAnimatedFile(Scene scene, AIScene aiScene) {
        if (aiScene.mNumAnimations() == 0) {
            System.err.println("the imported file does not contain any animations.");
            System.out.println(Assimp.aiGetErrorString());
            System.exit(0);
        }

        Mesh mesh = scene.meshes.get(0);

        int sizeOfVertexUnrigged = 11;
        int sizeOfVertex = sizeOfVertexUnrigged + Float.BYTES * 2;
        /**
         * position data 3f
         * normal   data 3f
         * tangent  data 3f
         * texcoord data 2f
         *
         * bone		info 4f
         * bone		info 4f
         */
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

            rawMeshData[index++] = 0;
            rawMeshData[index++] = 0;
            rawMeshData[index++] = 0;
            rawMeshData[index++] = 0;

            rawMeshData[index++] = 0;
            rawMeshData[index++] = 0;
            rawMeshData[index++] = 0;
            rawMeshData[index++] = 0;
        }

        HashMap<String, Integer> boneMap = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map0 = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map1 = new HashMap<>();

        for (int boneId = 0; boneId < mesh.getBones().length; boneId++) {
            Bone bone = Objects.requireNonNull(mesh.getBones()[boneId]);
            boneMap.put(bone.name, boneId);

            for (int weightId = 0; weightId < bone.weights.length; weightId++) {
                Bone.VertexWeight weight = bone.weights[weightId];
                int vertId = weight.vertexId;
                int vertOffset = vertId * sizeOfVertex;

                if (!bone_index_map0.containsKey(vertId)) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged)] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 2] = weight.weight;
                    bone_index_map0.put(vertId, 0);
                } else if (bone_index_map0.get(vertId) == 0) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 1] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 3] = weight.weight;
                    bone_index_map0.put(vertId, 1);
                } else if (!bone_index_map1.containsKey(vertId)) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 4] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 6] = weight.weight;
                    bone_index_map1.put(vertId, 0);
                } else if (bone_index_map1.get(vertId) == 0) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 5] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 7] = weight.weight;
                    bone_index_map1.put(vertId, 1);
                } else {
                    System.err.println("max 4 bones per vertex.");
                    System.exit(0);
                }
            }
        }

        Matrix4f inverseRootTransformation = scene.rootTransform;

        Bone[] bones = new Bone[boneMap.size()];
        for (int b = 0; b < mesh.getBones().length; b++) {
            bones[b] = mesh.getBones()[b];
        }

        AnimatedRenderObject component = new AnimatedRenderObject();
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

        component.addVertices(ShaderProgram.POKEMON_SHADER, vertBuffer, indices, textures.get(0));
        component.animation = AIAnimation.create(Objects.requireNonNull(aiScene.mAnimations()).get(0));
        component.bones = bones;
        component.boneTransforms = new Matrix4f[bones.length];
        component.root = aiScene.mRootNode();
        component.globalInverseTransform = inverseRootTransformation;
        return component;
    }
}