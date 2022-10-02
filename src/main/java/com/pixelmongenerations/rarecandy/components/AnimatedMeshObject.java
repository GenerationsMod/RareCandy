package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import me.hydos.gogoat.GLModel;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class AnimatedMeshObject extends MeshObject {
    public final Animation[] animations;
    public final ModelNode node;
    public Matrix4f[] boneTransforms;
    public int activeAnimation;
    public double animationTime;

    public AnimatedMeshObject(List<Material> glMaterials, Map<String, Material> variants, GLModel glModel, Pipeline pipeline, Animation[] animations, ModelNode node) {
        super(glMaterials, variants, glModel, pipeline);
        this.animations = animations;
        this.boneTransforms = new Matrix4f[0];
        this.node = node;
    }

    @Override
    public void update() {
        super.update();
        this.boneTransforms = animations[activeAnimation].getFrameTransform(animationTime, this.node);
    }
}
