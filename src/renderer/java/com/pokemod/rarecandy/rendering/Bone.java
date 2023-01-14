package com.pokemod.rarecandy.rendering;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Bone {
    public final String name;
    public final Matrix4f inversePoseMatrix;
    public final Vector3f posePosition = new Vector3f();
    public final Quaternionf poseRotation = new Quaternionf();
    public final Vector3f poseScale = new Vector3f(1,1,1);

    public Bone(NodeModel jointNode, Matrix4f inversePoseMatrix) {
        this.name = jointNode.getName();
        this.inversePoseMatrix = inversePoseMatrix;

        var pos = jointNode.getTranslation();
        if(pos != null) this.posePosition.set(jointNode.getTranslation());

        var rot = jointNode.getRotation();
        if(rot != null) this.poseRotation.set(rot[0], rot[1], rot[2], rot[3]);

        var scale = jointNode.getScale();
        if(scale != null) this.poseScale.set(jointNode.getScale());
    }

    @Override
    public String toString() {
        return name;
    }
}
