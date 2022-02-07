package com.pixelmongenerations.pixelmonassetutils.reader;

import com.pixelmongenerations.inception.rendering.Bone;
import com.pixelmongenerations.pixelmonassetutils.assimp.AssimpMaterial;
import com.pixelmongenerations.pixelmonassetutils.assimp.AssimpUtils;
import com.pixelmongenerations.pixelmonassetutils.scene.Scene;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.pixelmonassetutils.scene.objects.Mesh;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class GlbReader implements FileReader {
    private static final String BUNDLED_TEXTURE_CHAR = "*";

    @Deprecated //FIXME: this is just here so this doesnt error out when trying to merge with the renderer's reader.
    public AIScene rawScene;

    @Override
    public Scene read(TarFile file) throws IOException {
        AIScene aiScene = loadScene(file);
        this.rawScene = aiScene;

        List<Texture> textures = new ArrayList<>();
        PointerBuffer aiTextures = Objects.requireNonNull(aiScene.mTextures());
        for (int i = 0; i < aiTextures.capacity(); i++) {
            AITexture aiTexture = AITexture.create(aiTextures.get(i));
            textures.add(new Texture(aiTexture.pcDataCompressed(), aiTexture.mFilename().dataString()));
        }

        List<Material> materials = new ArrayList<>();
        PointerBuffer aiMaterials = Objects.requireNonNull(aiScene.mMaterials());
        Texture fallbackTexture = null;
        for (int i = 0; i < aiMaterials.capacity(); i++) {
            AssimpMaterial aiMaterial = new AssimpMaterial(AIMaterial.create(aiMaterials.get(i)));
            boolean hasDiffuse = Assimp.aiGetMaterialTextureCount(aiMaterial.material, Assimp.aiTextureType_DIFFUSE) >= 1;

            if (!hasDiffuse) {
                if (fallbackTexture == null) {
                    throw new RuntimeException("There is not a single texture in this entire mesh. What the fuck");
                }
                materials.add(i, new Material(fallbackTexture));
            } else {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    AIString path = AIString.calloc(stack);
                    Assimp.aiGetMaterialTexture(aiMaterial.material, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);

                    String texturePath = path.dataString();
                    if (texturePath.startsWith(BUNDLED_TEXTURE_CHAR)) {
                        Texture diffuse = textures.get(Integer.parseInt(texturePath.substring(1)));
                        materials.add(i, new Material(diffuse));

                        if (i == 0) {
                            // First material for some reason defines the texture for all other materials sometimes
                            fallbackTexture = diffuse;
                        }
                    }
                }
            }
        }

        List<Mesh> meshes = new ArrayList<>();
        PointerBuffer aiMeshes = requireNonNull(aiScene.mMeshes());
        for (int i = 0; i < aiMeshes.capacity(); i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            meshes.add(convert(aiMesh, materials));
        }

        return new Scene(meshes, AssimpUtils.from(requireNonNull(aiScene.mRootNode()).mTransformation()));
    }

    private AIScene loadScene(TarFile file) throws IOException {
        AIScene aiScene = null;
        for (TarArchiveEntry entry : file.getEntries()) {
            if (entry.getName().endsWith(".glb")) {
                byte[] bytes = file.getInputStream(entry).readAllBytes();
                ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
                buffer.put(bytes).flip();

                aiScene = Assimp.aiImportFileFromMemory(buffer, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_CalcTangentSpace | Assimp.aiProcess_LimitBoneWeights, "glb");
            }
        }

        if (aiScene == null) {
            throw new RuntimeException("Unable to locate .glb file to load! Reason: " + Assimp.aiGetErrorString());
        }

        return aiScene;
    }

    private Mesh convert(AIMesh mesh, List<Material> materials) {
        Vector3f[] vertices = new Vector3f[mesh.mNumVertices()];
        int[] indices = new int[mesh.mNumFaces() * 3]; // Each face is a triangle, so it must always be 3.
        Vector3f[] normals = new Vector3f[mesh.mNumVertices()];
        Vector2f[] texCoords = new Vector2f[mesh.mNumVertices() * 2];
        Vector3f[] tangents = new Vector3f[mesh.mNumVertices()];
        Bone[] bones = new Bone[mesh.mNumBones()];

        // Convert Vertices
        AIVector3D.Buffer aiVertices = requireNonNull(mesh.mVertices());
        for (int i = 0; i < aiVertices.capacity(); i++) {
            vertices[i] = AssimpUtils.from(aiVertices.get(i));
        }

        // Convert Indices
        AIFace.Buffer aiFaces = requireNonNull(mesh.mFaces());
        for (int i = 0; i < aiFaces.capacity(); i++) {
            AIFace face = aiFaces.get(i);

            IntBuffer pIndices = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); j++) {
                indices[j + (i * face.mNumIndices())] = (pIndices.get(j));
            }
        }

        // Convert Texture Coordinates
        AIVector3D.Buffer aiTexCoords = requireNonNull(mesh.mTextureCoords(0));
        for (int i = 0; i < aiTexCoords.capacity(); i++) {
            texCoords[i] = AssimpUtils.from3to2(aiTexCoords.get(i));
        }

        // Convert Normals
        AIVector3D.Buffer normalBuffer = requireNonNull(mesh.mNormals());
        for (int i = 0; i < normalBuffer.capacity(); i++) {
            normals[i] = AssimpUtils.from(normalBuffer.get(i));
        }

        // Convert Bones & Tangents if they exist.
        if (mesh.mBones() != null) {
            PointerBuffer aiBones = requireNonNull(mesh.mBones());
            for (int i = 0; i < aiBones.capacity(); i++) {
                AIBone bone = AIBone.create(aiBones.get(i));
                bones[i] = AssimpUtils.from(bone);
            }
        }

        AIVector3D.Buffer aiTangents = requireNonNull(mesh.mTangents());
        for (int i = 0; i < aiTangents.capacity(); i++) {
            tangents[i] = AssimpUtils.from(aiTangents.get(i));
        }

        return new Mesh(mesh.mName().dataString(), vertices, indices, normals, texCoords, tangents, bones, materials.get(mesh.mMaterialIndex()));
    }
}
