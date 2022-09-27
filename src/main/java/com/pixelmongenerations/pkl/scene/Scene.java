package com.pixelmongenerations.pkl.scene;

import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import org.joml.Matrix4f;

import java.util.List;

public class Scene {

    public final Matrix4f rootTransform;
    public final List<Mesh> meshes;
    public List<TextureReference> textures;

    public Scene(List<Mesh> meshes, Matrix4f rootTransform, List<TextureReference> textures) {
        this.meshes = meshes;
        this.rootTransform = rootTransform;
        this.textures = textures;
    }
}
