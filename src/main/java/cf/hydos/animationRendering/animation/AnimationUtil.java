package cf.hydos.animationRendering.animation;

import cf.hydos.pixelmonassetutils.reader.GlbReader;
import cf.hydos.pixelmonassetutils.scene.material.GlbTexture;
import cf.hydos.animationRendering.engine.core.Matrix4f;
import cf.hydos.animationRendering.engine.core.Util;
import cf.hydos.animationRendering.engine.rendering.Texture;
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

/*        AIScene scene = Assimp.aiImportFile(file.toString(),
                Assimp.aiProcess_Triangulate |
                        Assimp.aiProcess_GenSmoothNormals |
                        Assimp.aiProcess_FlipUVs |
                        Assimp.aiProcess_CalcTangentSpace |
                        Assimp.aiProcess_LimitBoneWeights
        );*/

        if (scene == null || scene.mNumAnimations() == 0) {
            System.err.println("the imported file does not contain any animations.");
            System.out.println(Assimp.aiGetErrorString());
            System.exit(0);
        }

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));

        int sizeOfVertex = 19;
        int sizeOfVertexUnrigged = 11;
        /**
         * position data 3f
         * normal   data 3f
         * tangent  data 3f
         * texcoord data 2f
         *
         * bone		info 4f
         * bone		info 4f
         */
        float[] array = new float[mesh.mNumVertices() * sizeOfVertex];
        int index = 0;

        for (int v = 0; v < mesh.mNumVertices(); v++) {
            AIVector3D position = mesh.mVertices().get(v);
            AIVector3D normal = mesh.mNormals().get(v);
            AIVector3D tangent = mesh.mTangents().get(v);
            AIVector3D texCoord = mesh.mTextureCoords(0).get(v);
            /**
             * The above assumes that the program has texture coordinates, if it doesn't the program will throw a null pointer exception.
             */

            array[index++] = position.x();
            array[index++] = position.y();
            array[index++] = position.z();

            array[index++] = texCoord.x();
            array[index++] = texCoord.y();

            array[index++] = normal.x();
            array[index++] = normal.y();
            array[index++] = normal.z();

            array[index++] = tangent.x();
            array[index++] = tangent.y();
            array[index++] = tangent.z();

            array[index++] = 0;
            array[index++] = 0;
            array[index++] = 0;
            array[index++] = 0;

            array[index++] = 0;
            array[index++] = 0;
            array[index++] = 0;
            array[index++] = 0;
        }

        IntBuffer indices = Util.CreateIntBuffer(mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());

        for (int f = 0; f < mesh.mNumFaces(); f++) {
            AIFace face = mesh.mFaces().get(f);
            for (int ind = 0; ind < face.mNumIndices(); ind++)
                indices.put(face.mIndices().get(ind));
        }

        HashMap<String, Integer> boneMap = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map0 = new HashMap<>();
        HashMap<Integer, Integer> bone_index_map1 = new HashMap<>();

        for (int b = 0; b < mesh.mNumBones(); b++) {
            AIBone bone = AIBone.create(mesh.mBones().get(b));
            boneMap.put(bone.mName().dataString(), b);

            for (int w = 0; w < bone.mNumWeights(); w++) {
                AIVertexWeight weight = bone.mWeights().get(w);
                int vertexIndex = weight.mVertexId();
                int findex = vertexIndex * sizeOfVertex;

                if (!bone_index_map0.containsKey(vertexIndex)) {
                    array[(findex + sizeOfVertexUnrigged)] = b;
                    array[(findex + sizeOfVertexUnrigged) + 2] = weight.mWeight();
                    bone_index_map0.put(vertexIndex, 0);
                } else if (bone_index_map0.get(vertexIndex) == 0) {
                    array[(findex + sizeOfVertexUnrigged) + 1] = b;
                    array[(findex + sizeOfVertexUnrigged) + 3] = weight.mWeight();
                    bone_index_map0.put(vertexIndex, 1);
                } else if (!bone_index_map1.containsKey(vertexIndex)) {
                    array[(findex + sizeOfVertexUnrigged) + 4] = b;
                    array[(findex + sizeOfVertexUnrigged) + 6] = weight.mWeight();
                    bone_index_map1.put(vertexIndex, 0);
                } else if (bone_index_map1.get(vertexIndex) == 0) {
                    array[(findex + sizeOfVertexUnrigged) + 5] = b;
                    array[(findex + sizeOfVertexUnrigged) + 7] = weight.mWeight();
                    bone_index_map1.put(vertexIndex, 1);
                } else {
                    System.err.println("max 4 bones per vertex.");
                    System.exit(0);
                }
            }
        }

        AIMatrix4x4 inverseRootTransform = scene.mRootNode().mTransformation();
        Matrix4f inverseRootTransformation = new Matrix4f().fromAssimp(inverseRootTransform);

        Bone[] bones = new Bone[boneMap.size()];

        for (int b = 0; b < mesh.mNumBones(); b++) {
            AIBone bone = AIBone.create(mesh.mBones().get(b));
            bones[b] = new Bone();

            bones[b].name = bone.mName().dataString();
            bones[b].offsetMatrix = new Matrix4f().fromAssimp(bone.mOffsetMatrix());
        }

        AnimatedComponent component = new AnimatedComponent();
        FloatBuffer vertices = Util.CreateFloatBuffer(array.length);

        for (float v : array) vertices.put(v);
        vertices.flip();
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


        component.AddVertices(vertices, indices, new Texture(textures.get(0)));

        component.animation = AIAnimation.create(scene.mAnimations().get(0));
        component.bones = bones;
        component.boneTransforms = new Matrix4f[bones.length];
        component.root = scene.mRootNode();
        component.globalInverseTransform = inverseRootTransformation;

        return component;
    }
}
