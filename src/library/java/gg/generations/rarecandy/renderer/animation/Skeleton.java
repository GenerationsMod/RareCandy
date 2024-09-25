package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.assimp.AIBone;
import gg.generations.rarecandy.assimp.AIMesh;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.rendering.Bone;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Skeleton {
    public final Map<String, ModelNode> jointMap;
    public Bone[] bones;
    public Map<String, Integer> boneIdMap;
    public final Map<String, Bone> boneMap;
    public final ModelNode rootNode;

    public Skeleton(Skeleton skeleton) {
        this.bones = skeleton.bones;
        this.jointMap = skeleton.jointMap;
        this.boneMap = skeleton.boneMap;
        this.rootNode = skeleton.rootNode;
        this.boneIdMap = skeleton.boneIdMap;
    }


    public Skeleton(ModelNode rootNode, AIMesh[] meshes, boolean excludeMeshes) {
        this.rootNode = rootNode;
        this.boneIdMap = new HashMap<>();
        var jointList = new ArrayList<ModelNode>();

        var meshNames = excludeMeshes ? Stream.of(meshes).map(a -> a.mName().dataString()).collect(Collectors.toSet()) : Set.<String>of();
        populateJoints(rootNode, jointList, meshNames);

        var boneCount = jointList.size();
        this.bones = new Bone[boneCount];

        jointMap = jointList.stream().collect(Collectors.toMap(a -> a.name, a -> a));
        boneMap = jointMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> new Bone(a.getValue().name)));
        for (int i = 0; i < jointList.size(); i++) {
            var name = jointList.get(i).name;
            bones[i] = boneMap.get(name);
            boneIdMap.put(name, i);
        }

        for (AIMesh aiMesh : meshes) {
            processBones(aiMesh);
        }
    }

    private void processBones(AIMesh mesh) {

        if (mesh.mBones() != null) {
            var aiBones = requireNonNull(mesh.mBones());

            for (int i = 0; i < aiBones.capacity(); i++) {
                configure(AIBone.create(aiBones.get(i)));
            }
        }
    }

    public void configure(AIBone bone) {
        var b = boneMap.get(bone.mName().dataString());
        ModelNode.from(b.inverseBindMatrix, bone.mOffsetMatrix());
        b.restPose.set(b.inverseBindMatrix).invert();
    }

    private void populateJoints(ModelNode joint, ArrayList<ModelNode> jointList, Set<String> meshNamesToExclude) {
        if(!meshNamesToExclude.contains(joint.name)) jointList.add(joint);
        for (var child : joint.children) populateJoints(child, jointList, meshNamesToExclude);
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
