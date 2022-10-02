package com.pixelmongenerations.rarecandy.rendering;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;

public class Bone {
    public final String name;
    public final Matrix4f offsetMatrix;

    public Bone(NodeModel jointNode) {
        this.name = jointNode.getName();
        this.offsetMatrix = new Matrix4f();

        var translation = jointNode.getTranslation();
        if(translation != null) this.offsetMatrix.translate(translation[0], translation[1], translation[2]);

        var rotation = jointNode.getRotation();
        if(rotation != null) this.offsetMatrix.rotate(rotation[0], rotation[1], rotation[2], rotation[3]);

        var scale = jointNode.getScale();
        if(scale != null) this.offsetMatrix.scale(scale[0], scale[1], scale[2]);
    }

    @Override
    public String toString() {
        return name;
    }
}
