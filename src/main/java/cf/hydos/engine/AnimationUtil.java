package cf.hydos.engine;

import cf.hydos.engine.components.AnimatedComponent;
import cf.hydos.engine.rendering.Bone;
import cf.hydos.engine.rendering.Texture;
import cf.hydos.pixelmonassetutils.AssimpUtils;
import cf.hydos.pixelmonassetutils.reader.GlbReader;
import cf.hydos.pixelmonassetutils.scene.material.GlbTexture;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnimationUtil {
    public static AnimatedComponent loadAnimatedFile(File file) {
        byte[] bytes = new byte[0];
        String extension = null;
        try {
            String path = file.toString().replace("\\", "/").replace("./res", "");
            extension = path.split("\\.")[1];
            bytes = AnimationUtil.class.getResourceAsStream(path).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes).flip();


        AIScene scene = Assimp.aiImportFileFromMemory(buffer, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_CalcTangentSpace | Assimp.aiProcess_LimitBoneWeights, extension);

        if (scene == null || scene.mNumAnimations() == 0) {
            System.err.println("the imported file does not contain any animations.");
            System.out.println(Assimp.aiGetErrorString());
            System.exit(0);
        }

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));

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
        float[] rawMeshData = new float[mesh.mNumVertices() * sizeOfVertex];
        int index = 0;

        for (int v = 0; v < mesh.mNumVertices(); v++) {
            AIVector3D position = mesh.mVertices().get(v);
            AIVector3D normal = mesh.mNormals().get(v);
            AIVector3D tangent = mesh.mTangents().get(v);
            AIVector3D texCoord = mesh.mTextureCoords(0).get(v);

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

        IntBuffer indices = BufferUtils.createIntBuffer(mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());

        for (int f = 0; f < mesh.mNumFaces(); f++) {
            AIFace face = mesh.mFaces().get(f);
            for (int ind = 0; ind < face.mNumIndices(); ind++)
                indices.put(face.mIndices().get(ind));
        }

        HashMap<String, Integer> boneMap = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map0 = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map1 = new HashMap<>();

        for (int boneId = 0; boneId < mesh.mNumBones(); boneId++) {
            AIBone bone = AIBone.create(mesh.mBones().get(boneId));
            boneMap.put(bone.mName().dataString(), boneId);

            for (int weightId = 0; weightId < bone.mNumWeights(); weightId++) {
                AIVertexWeight weight = bone.mWeights().get(weightId);
                int vertId = weight.mVertexId();
                int vertOffset = vertId * sizeOfVertex;

                if (!bone_index_map0.containsKey(vertId)) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged)] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 2] = weight.mWeight();
                    bone_index_map0.put(vertId, 0);
                } else if (bone_index_map0.get(vertId) == 0) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 1] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 3] = weight.mWeight();
                    bone_index_map0.put(vertId, 1);
                } else if (!bone_index_map1.containsKey(vertId)) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 4] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 6] = weight.mWeight();
                    bone_index_map1.put(vertId, 0);
                } else if (bone_index_map1.get(vertId) == 0) {
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 5] = boneId;
                    rawMeshData[(vertOffset + sizeOfVertexUnrigged) + 7] = weight.mWeight();
                    bone_index_map1.put(vertId, 1);
                } else {
                    System.err.println("max 4 bones per vertex.");
                    System.exit(0);
                }
            }
        }

        AIMatrix4x4 inverseRootTransform = scene.mRootNode().mTransformation();
        Matrix4f inverseRootTransformation = AssimpUtils.from(inverseRootTransform);

        Bone[] bones = new Bone[boneMap.size()];

        for (int b = 0; b < mesh.mNumBones(); b++) {
            AIBone bone = AIBone.create(mesh.mBones().get(b));
            bones[b] = new Bone();

            bones[b].name = bone.mName().dataString();
            bones[b].offsetMatrix = AssimpUtils.from(bone.mOffsetMatrix());
        }

        AnimatedComponent component = new AnimatedComponent();
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);

        for (float v : rawMeshData) vertBuffer.put(v);
        vertBuffer.flip();
        indices.flip();

        List<GlbReader.AssimpMaterial> rawMaterials = new ArrayList<>();
        List<AITexture> rawTextures = new ArrayList<>();

        // Get materials
        PointerBuffer pMaterials = scene.mMaterials();
        if (pMaterials != null) {
            for (int i = 0; i < pMaterials.capacity(); i++) {
                rawMaterials.add(new GlbReader.AssimpMaterial(AIMaterial.create(pMaterials.get(i))));
            }
        } else {
            throw new RuntimeException("Can't handle models with no materials. We can't guess how you want us to render the object?");
        }

        // Retrieve Textures
        PointerBuffer pTextures = scene.mTextures();
        if (pTextures != null) {
            for (int i = 0; i < scene.mNumTextures(); i++) {
                rawTextures.add(AITexture.create(pTextures.get(i)));
            }
        } else {
            throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
        }

        // Try to load the textures into rosella
        List<GlbTexture> textures = new ArrayList<>();
        for (AITexture rawTexture : rawTextures) {
            if (rawTexture.mHeight() > 0) {
                throw new RuntimeException(".glb file had texture with height of 0");
            } else {
                textures.add(new GlbTexture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
            }
        }


        component.AddVertices(vertBuffer, indices, new Texture(textures.get(0)));

        component.animation = AIAnimation.create(scene.mAnimations().get(0));
        component.bones = bones;
        component.boneTransforms = new Matrix4f[bones.length];
        component.root = scene.mRootNode();
        component.globalInverseTransform = inverseRootTransformation;

        return component;
    }
}
