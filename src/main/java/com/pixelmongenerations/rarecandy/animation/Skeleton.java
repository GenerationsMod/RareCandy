package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import de.javagl.jgltf.model.SkinModel;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

public class Skeleton {
    public final Bone[] boneArray;
    public final Map<String, Bone> boneMap;
    public final ModelNode rootNode;

    public Skeleton(SkinModel skeleton) {
        var boneCount = skeleton.getJoints().size();
        this.boneArray = new Bone[boneCount];
        this.boneMap = new HashMap<>(boneCount);
        this.rootNode = new ModelNode(skeleton.getJoints().get(0).getParent());

        float[] array = new float[16];

        for (var i = 0; i < skeleton.getJoints().size(); i++) {
            var jointNode = skeleton.getJoints().get(i);
            var bone = new Bone(jointNode, new Matrix4f().set(skeleton.getInverseBindMatrix(i, array)));
            this.boneArray[i] = bone;
            this.boneMap.put(jointNode.getName(), bone);
        }
    }

    public Bone get(String name) {
        return boneMap.get(name);
    }

    public Bone get(int id) {
        return boneArray[id];
    }

    public int getId(Bone bone) {
        for (int i = 0; i < boneArray.length; i++) {
            if (bone.equals(boneArray[i])) return i;
        }

        return 0;
    }
}
