package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.rarecandy.rendering.Bone;

import java.util.HashMap;
import java.util.Map;

public class Bones {
    public final Bone[] boneArray;
    public final Map<String, Bone> boneMap;

    public Bones(Bone[] array) {
        this.boneArray = array;
        this.boneMap = new HashMap<>(array.length);

        for (Bone bone : array) {
            this.boneMap.put(bone.name, bone);
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
