package com.pixelmongenerations.pkl.scene;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.pkl.reader.TextureReference;
import de.javagl.jgltf.model.GltfModel;

import java.util.List;

public class Scene {
    public final GltfModel model;
    public final ModelNode rootNode;
    public List<TextureReference> textures;

    public Scene(GltfModel model, ModelNode rootNode, List<TextureReference> textures) {
        this.model = model;
        this.rootNode = rootNode;
        this.textures = textures;
    }
}
