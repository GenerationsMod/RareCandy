package com.pixelmongenerations.pixelmonassetutils.scene;

import com.pixelmongenerations.pixelmonassetutils.scene.objects.Mesh;
import org.joml.Matrix4f;

import java.util.List;

public class Scene {

    public final Matrix4f rootTransform;
    public final List<Mesh> meshes;

    public Scene(List<Mesh> meshes, Matrix4f rootTransform) {
        this.meshes = meshes;
        this.rootTransform = rootTransform;
    }
}
