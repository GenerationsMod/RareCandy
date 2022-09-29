package com.pixelmongenerations.pkl;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public final Matrix4f transform;
    public final List<ModelNode> children = new ArrayList<>();

    public ModelNode(List<NodeModel> nodeModels) {
        NodeModel rootNode = null;

        for (var node : nodeModels) {
            if (node.getParent() == null) {
                rootNode = node;
            }
        }

        this.name = rootNode.getName();
        this.transform = new Matrix4f(FloatBuffer.wrap(rootNode.getTranslation()));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
