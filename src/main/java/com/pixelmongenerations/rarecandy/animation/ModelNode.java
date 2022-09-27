package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.assimp.AssimpUtils;
import org.joml.Matrix4f;
import org.lwjgl.assimp.AINode;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public final Matrix4f transform;
    public final List<ModelNode> children = new ArrayList<>();

    public ModelNode(AINode aiNode) {
        this.name = aiNode.mName().dataString();
        this.transform = AssimpUtils.from(aiNode.mTransformation());

        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            children.add(new ModelNode(AINode.create(aiNode.mChildren().get(i))));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
