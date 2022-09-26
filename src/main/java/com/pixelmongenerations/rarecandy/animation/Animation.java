package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.assimp.AssimpUtils;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;

public class Animation {
    private static final long TIMER = System.currentTimeMillis();
    private final AIAnimation animation;
    private final Bone[] bones;
    private final AINode rootNode;
    public final String name;

    public Animation(AIAnimation animation, Bone[] bones, AINode rootNode) {
        this.animation = animation;
        this.bones = bones;
        this.rootNode = rootNode;
        this.name = animation.mName().dataString();
    }

    public Matrix4f[] getTransformsForFrame(int offset) {
        var boneTransforms = new Matrix4f[bones.length];
        var timeSinceStart = (double) (System.currentTimeMillis() - TIMER) / 1000.0 + offset;
        var tps = (float) (animation.mTicksPerSecond() != 0 ? animation.mTicksPerSecond() : 25.0f);
        var tickTime = timeSinceStart * tps;
        var animTime = (tickTime % (float) animation.mDuration());

        readNodeHierarchy((float) animTime, rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    private Bone findBone(String name) {
        for (var bone : bones) if (bone.name.equals(name)) return bone;
        return null;
    }

    protected void readNodeHierarchy(float animTime, AINode pNode, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = pNode.mName().dataString();
        var transform = AssimpUtils.from(pNode.mTransformation());
        var pNodeAnim = findNodeAnim(animation, name);

        if (pNodeAnim != null) {
            var scale = new Vector3f(1, 1, 1);
            scale = AnimationMath.calcInterpolatedScaling(scale, animTime, pNodeAnim);
            var ScalingM = new Matrix4f().identity().scale(scale.x(), scale.y(), scale.z());

            var rotation = new Quaternionf(0, 0, 0, 0);
            AnimationMath.calcInterpolatedRotation(rotation, animTime, pNodeAnim);
            var RotationM = rotation.get(new Matrix4f());

            var translation = new Vector3f(0, 0, 0);
            AnimationMath.calcInterpolatedPosition(translation, animTime, pNodeAnim);
            var TranslationM = new Matrix4f().identity().translate(translation.x(), translation.y(), translation.z());

            // Combine the above transformations
            transform = new Matrix4f(TranslationM).mul(new Matrix4f(RotationM)).mul(new Matrix4f(ScalingM));
        }

        var globalTransform = new Matrix4f(parentTransform).mul(transform);
        var bone = findBone(name);

        if(bone != null) boneTransforms[getBoneId(bone)] = new Matrix4f().identity().mul(new Matrix4f(globalTransform)).mul(bone.offsetMatrix);

        for (var i = 0; i < pNode.mNumChildren(); i++) {
            readNodeHierarchy(animTime, AINode.create(pNode.mChildren().get(i)), globalTransform, boneTransforms);
        }
    }

    private int getBoneId(Bone bone) {
        for (var i = 0; i < this.bones.length; i++) if (bone.equals(this.bones[i])) return i;
        throw new RuntimeException("Bone is not in bone array");
    }

    public AINodeAnim findNodeAnim(AIAnimation pAnimation, String NodeName) {
        for (int i = 0; i < pAnimation.mNumChannels(); i++) {
            AINodeAnim pNodeAnim = AINodeAnim.create(pAnimation.mChannels().get(i));

            if (pNodeAnim.mNodeName().dataString().equals(NodeName)) return pNodeAnim;
        }

        return null;
    }

    @Override
    public String toString() {
        return "Animation{" + "rootNode=" + rootNode + ", name='" + name + '\'' + '}';
    }
}
