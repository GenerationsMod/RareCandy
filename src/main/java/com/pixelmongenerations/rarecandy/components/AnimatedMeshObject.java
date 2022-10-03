package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import me.hydos.gogoat.GLModel;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimatedMeshObject extends MeshObject {
    public final Map<String, Animation> animations;
    public Matrix4f[] boneTransforms;
    public String activeAnimation;
    public double animationTime;

    public AnimatedMeshObject(List<Material> glMaterials, Map<String, Material> variants, GLModel glModel, Pipeline pipeline, Map<String, Animation> animations) {
        super(glMaterials, variants, glModel, pipeline);
        this.animations = animations;
        this.boneTransforms = new Matrix4f[0];
        this.activeAnimation = new ArrayList<>(animations.keySet()).get(0);
        setReady();
    }

    @Override
    public void update() {
        super.update();
        this.boneTransforms = animations.get(activeAnimation).getFrameTransform(animationTime);
    }
}
