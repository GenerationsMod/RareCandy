package gg.generations.rarecandy.renderer.animation;

import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SkinModel;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Skeleton {
    public final Map<String, ModelNode> jointMap;
    public final Bone[] bones;
    public final Map<String, Bone> boneMap;
    public final ModelNode rootNode;

    public Skeleton(Skeleton skeleton) {
        this.bones = skeleton.bones;
        this.jointMap = skeleton.jointMap;
        this.boneMap = skeleton.boneMap;
        this.rootNode = skeleton.rootNode;
    }


    public Skeleton(List<NodeModel> nodes, SkinModel skeleton) {
        this.rootNode = new ModelNode(findRoot(nodes.get(0)), null);
        var jointList = new ArrayList<ModelNode>();
        populateJoints(rootNode, jointList);

        jointMap = jointList.stream().collect(Collectors.toMap(a -> a.name, a -> a));

        var boneCount = skeleton.getJoints().size();
        this.bones = new Bone[boneCount];
        this.boneMap = new HashMap<>(boneCount);

        var array = new float[16];


        for (int i = 0; i < boneCount; i++) {
            var name = skeleton.getJoints().get(i).getName();
            var invertBindPose = skeleton.getInverseBindMatrix(i, array);
            Matrix4f inverseBindMatrix = new Matrix4f().set(invertBindPose);

            var bone = new Bone(name, inverseBindMatrix);
            this.bones[i] = bone;
            this.boneMap.put(name, bone);
        }
    }

    private void populateJoints(ModelNode joint, List<ModelNode> jointList) {
        jointList.add(joint);
        for (var child : joint.children) {
            populateJoints(child, jointList);
        }
    }

    private NodeModel findRoot(NodeModel root) {
        while (root.getParent() != null) {
            root = root.getParent();
        }

        return root;
    }

    public Bone get(String name) {
        return boneMap.get(name);
    }

    public Bone get(int id) {
        if (id > bones.length)
            throw new RuntimeException("Animation is referencing bones which are out of bounds. Model is missing bone " + id);
        return bones[id];
    }

    public String getName(int id) {
        var bone = get(id);

        for (var entry : boneMap.entrySet()) {
            if (entry.getValue().equals(bone)) return entry.getKey();
        }

        return "";
    }

    public int getId(Bone bone) {
        for (int i = 0; i < bones.length; i++) {
            if (bone.equals(bones[i])) return i;
        }

        return 0;
    }
}
