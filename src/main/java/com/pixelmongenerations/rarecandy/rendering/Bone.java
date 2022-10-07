package com.pixelmongenerations.rarecandy.rendering;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;

public class Bone {
    public final String name;
    public final Matrix4f inversePoseMatrix;

    public Bone(NodeModel jointNode, Matrix4f inversePoseMatrix) {
        this.name = jointNode.getName();
        this.inversePoseMatrix = inversePoseMatrix;
    }

    @Override
    public String toString() {
        return name;
    }
}
