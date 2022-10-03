package com.pixelmongenerations.pkl.scene;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.pkl.reader.TextureReference;
import de.javagl.jgltf.model.GltfModel;

import java.util.List;

public class Scene {
    public final GltfModel model;
    public List<TextureReference> textures;

    public Scene(GltfModel model, List<TextureReference> textures) {
        this.model = model;
        this.textures = textures;
    }
}
