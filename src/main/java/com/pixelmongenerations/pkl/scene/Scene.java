package com.pixelmongenerations.pkl.scene;

import com.pixelmongenerations.pkl.scene.material.Texture;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import org.joml.Matrix4f;

import java.util.List;

public class Scene {

    public final Matrix4f rootTransform;
    public final List<Mesh> meshes;
    public List<Texture> textures;

    public Scene(List<Mesh> meshes, Matrix4f rootTransform, List<Texture> textures) {
        this.meshes = meshes;
        this.rootTransform = rootTransform;
        this.textures = textures;
    }
}
