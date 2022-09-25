package com.pixelmongenerations.pkl.reader;

import com.pixelmongenerations.pkl.assimp.AssimpUtils;
import com.pixelmongenerations.pkl.scene.Scene;
import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.pkl.scene.material.Texture;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class GlbReader implements FileReader {

    public GltfModelReader reader = new GltfModelReader();
    public AIScene rawScene;

    @Override
    public Scene read(TarFile file) throws IOException {
        Pair<AIScene, GltfModel> pair = loadScene(file);
        return read(pair);
    }

    public Scene read(InputStream file) throws IOException {
        Pair<AIScene, GltfModel> pair = loadScene(file.readAllBytes());
        return read(pair);
    }

    @NotNull
    private Scene read(Pair<AIScene, GltfModel> pair) {
        AIScene aiScene = pair.a();
        GltfModel model = pair.b();
        this.rawScene = aiScene;

        List<Texture> textures = model.getTextureModels().stream().map(raw -> new Texture(raw.getImageModel().getImageData(), raw.getImageModel().getName())).toList();
        List<Material> materials = textures.stream().map(Material::new).collect(Collectors.toList());

        List<Mesh> meshes = new ArrayList<>();
        PointerBuffer aiMeshes = requireNonNull(aiScene.mMeshes());
        for (int i = 0; i < aiMeshes.capacity(); i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            meshes.add(convert(aiMesh, materials));
        }

        return new Scene(meshes, AssimpUtils.from(requireNonNull(aiScene.mRootNode()).mTransformation()), textures);
    }

    private Pair<AIScene, GltfModel> loadScene(TarFile file) throws IOException {
        for (TarArchiveEntry entry : file.getEntries()) {
            if (entry.getName().endsWith(".glb")) {
                return loadScene(file.getInputStream(entry).readAllBytes());
            }
        }

        throw new RuntimeException("pk format archive contained no glb formatted files");
    }

    private Pair<AIScene, GltfModel> loadScene(byte[] bytes) throws IOException {
        var model = reader.readWithoutReferences(new ByteArrayInputStream(bytes));

        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes).flip();
        var aiScene = Assimp.aiImportFileFromMemory(buffer, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_CalcTangentSpace | Assimp.aiProcess_LimitBoneWeights, "glb");

        if (aiScene == null || model == null) {
            throw new RuntimeException("Unable to locate .glb file to load! Reason: " + Assimp.aiGetErrorString());
        }

        return new Pair<>(aiScene, model);
    }

    private record Pair<A, B>(A a, B b) {
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

        var material = mesh.mMaterialIndex() >= materials.size() ? null : materials.get(mesh.mMaterialIndex());

        return new Mesh(mesh.mName().dataString(), vertices, indices, normals, texCoords, tangents, bones, material);
    }
}
