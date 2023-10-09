package gg.generationsmod.rarecandy.model.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Skeleton {
    public final BoneNode[] nodes;
    public final Map<String, Bone> boneMap;
    public final Map<Bone, Integer> cachedBoneIds = new HashMap<>();
    public final BoneNode rootNode;
    public Bone[] bones;

    public Skeleton(BoneNode root) {
        var jointList = new ArrayList<BoneNode>();
        populateJoints(root, jointList);
        this.rootNode = root;
        this.nodes = new BoneNode[jointList.size()];
        this.boneMap = new HashMap<>();

        for (int i = 0; i < jointList.size(); i++) {
            var joint = jointList.get(i);
            this.nodes[i] = joint;
        }
    }

    private static void populateJoints(BoneNode joint, ArrayList<BoneNode> jointList) {
        jointList.add(joint);
        for (var child : joint.children) populateJoints(child, jointList);
    }

    public int getId(Bone bone) {
        if (cachedBoneIds.get(bone) == null) {
            for (int i = 0; i < bones.length; i++)
                if (bone.name.equals(bones[i].name)) return i;

            System.out.println("Something is about to go wrong. Bone cannot be found inside of skeleton \"" + bone.name + "\"");
            cachedBoneIds.put(bone, 0);
        }

        return cachedBoneIds.get(bone);
    }

    public void store(Bone[] bones) {
        for (var bone : bones) boneMap.put(bone.name, bone);
    }

    public void calculateBoneData() {
        this.bones = boneMap.values().toArray(Bone[]::new);
    }

    public Bone getBone(String name) {
        return boneMap.get(name);
    }
}
