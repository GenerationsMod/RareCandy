package gg.generations.rarecandy.renderer.animation;

import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SkinModel;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.rendering.Bone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Skeleton {
    public final Map<String, ModelNode> jointMap;
    public Bone[] bones;
    public final Map<String, Bone> boneMap;
    public final ModelNode rootNode;

    public Skeleton(Skeleton skeleton) {
        this.bones = skeleton.bones;
        this.jointMap = skeleton.jointMap;
        this.boneMap = skeleton.boneMap;
        this.rootNode = skeleton.rootNode;
    }


    public Skeleton(ModelNode rootNode) {
        this.rootNode = rootNode;
        var jointList = new ArrayList<ModelNode>();
        populateJoints(rootNode, jointList);

        jointMap = jointList.stream().collect(Collectors.toMap(a -> a.name, a -> a));

        var boneCount = jointList.size();
        this.bones = new Bone[boneCount];
        this.boneMap = new HashMap<>(boneCount);
    }

    private static void populateJoints(ModelNode joint, ArrayList<ModelNode> jointList) {
        jointList.add(joint);
        for (var child : joint.children) populateJoints(child, jointList);
    }

    public void store(Bone[] bones) {
        for (var bone : bones) {
            boneMap.put(bone.name, bone);
        }
    }

    public void calculateBoneData() {
        this.bones = boneMap.values().toArray(Bone[]::new);
    }


    private NodeModel findRoot(SkinModel skeleton) {
        if(skeleton.getSkeleton() != null) return skeleton.getSkeleton();

        var root = skeleton.getJoints().get(0);

        while(root.getParent() != null) {
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
        if(bone != null) {
            for (int i = 0; i < bones.length; i++) {
                if (bone.equals(bones[i])) return i;
            }
        }

        return 0;
    }

    public int getId(String name) {
        return getId(get(name));
    }
}
