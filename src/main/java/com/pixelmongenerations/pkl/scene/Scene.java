package com.pixelmongenerations.pkl.scene;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import de.javagl.jgltf.model.GltfModel;

import java.util.List;

public class Scene {
    public final GltfModel gltf;
    public final ModelNode rootNode;
    public final List<Mesh> meshes;
    public List<TextureReference> textures;

    public Scene(List<Mesh> meshes, GltfModel gltf, ModelNode rootNode, List<TextureReference> textures) {
        this.meshes = meshes;
        this.gltf = gltf;
        this.rootNode = rootNode;
        this.textures = textures;
    }
}
