package com.pixelmongenerations.pkl.reader;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.pkl.scene.Scene;
import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlbReader {

    public final GltfModelReader reader = new GltfModelReader();

    public Scene read(TarFile file) throws IOException {
        var model = loadScene(file);
        return read(model);
    }

    public Scene read(InputStream file) throws IOException {
        var model = loadScene(file.readAllBytes());
        return read(model);
    }

    @NotNull
    private Scene read(GltfModel model) {
        var textures = model.getTextureModels().stream().map(raw -> new TextureReference(PixelDatas.create(raw.getImageModel().getImageData()), raw.getImageModel().getName())).toList();
        var materials = textures.stream().map(Material::new).collect(Collectors.toList());
        var meshes = new ArrayList<Mesh>();

        for (var mesh : model.getMeshModels()) {
            for (var primitiveModel : mesh.getMeshPrimitiveModels()) {
                meshes.add(convert(primitiveModel, materials));
            }
        }

        return new Scene(meshes, model, new ModelNode(model.getNodeModels()), textures);
    }

    private GltfModel loadScene(TarFile file) throws IOException {
        for (TarArchiveEntry entry : file.getEntries()) {
            if (entry.getName().endsWith(".glb")) {
                return loadScene(file.getInputStream(entry).readAllBytes());
            }
        }

        throw new RuntimeException("pk format archive contained no glb formatted files");
    }

    private GltfModel loadScene(byte[] bytes) throws IOException {
        return reader.readWithoutReferences(new ByteArrayInputStream(bytes));
    }

    private Mesh convert(MeshPrimitiveModel mesh, List<Material> materials) {
        var indexAccess = mesh.getIndices();
        var indexBuffer = indexAccess.getBufferViewModel().getBufferViewData().asIntBuffer();
        var indices = new int[indexAccess.getCount()];
        for (var i = 0; i < indices.length; i++) {
            System.out.println("A" + i);
            if(i == 8310) {
                System.out.println("break");
            }
            indices[i] = indexBuffer.get(i);
            System.out.println("B" + i);
        }

        System.out.println("C");

        var positionsAccess = mesh.getAttributes().get("POSITION");
        var positionBuffer = positionsAccess.getBufferViewModel().getBufferViewData().asFloatBuffer();
        var vertices = new Vector3f[positionsAccess.getCount()];
        for (var i = 0; i < vertices.length; i++) {
            vertices[i] = new Vector3f(positionBuffer.get(i * 3), positionBuffer.get(i * 3 + 1), positionBuffer.get(i * 3 + 2));
        }

        /*
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
        }*/

        return null;
        //return new Mesh(mesh.mName().dataString(), vertices, indices, normals, texCoords, tangents, bones);
    }
}
