package com.pixelmongenerations.pkl.reader;

import com.pixelmongenerations.pkl.JGltfUtils;
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
                meshes.add(convert(mesh.getName() + "_" + primitiveModel.hashCode(), primitiveModel, materials));
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

    private Mesh convert(String name, MeshPrimitiveModel mesh, List<Material> materials) {
        var indices = JGltfUtils.readShort1(mesh.getIndices().getAccessorData());
        var vertices = JGltfUtils.readFloat3(mesh.getAttributes().get("POSITION").getAccessorData());
        var normals = JGltfUtils.readFloat3(mesh.getAttributes().get("NORMAL").getAccessorData());
        var texCoords = JGltfUtils.readFloat2(mesh.getAttributes().get("TEXCOORD_0").getAccessorData());
        System.out.println(name);

        /*
        Vector3f[] tangents = new Vector3f[mesh.mNumVertices()];
        Bone[] bones = new Bone[mesh.mNumBones()];

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

        return new Mesh(name, vertices, indices, normals, texCoords, null, null);
    }
}
