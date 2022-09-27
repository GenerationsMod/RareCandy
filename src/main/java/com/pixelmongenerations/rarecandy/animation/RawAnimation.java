package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.rarecandy.rendering.Bone;
import org.lwjgl.assimp.AIAnimation;

public class RawAnimation {
    protected final AIAnimation aiAnim;
    protected final Bone[] bones;
    protected final String name;

    public RawAnimation(AIAnimation animation, Bone[] bones) {
        this.aiAnim = animation;
        this.bones = bones;
        this.name = animation.mName().dataString();
    }

    @Override
    public String toString() {
        return "RawAnimation{ name='" + name + '\'' + '}';
    }
}
