package com.pixelmongenerations.pkl;

import de.javagl.jgltf.model.NodeModel;
import me.hydos.gogoat.util.DataUtils;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public final Matrix4f transform;
    public final List<ModelNode> children = new ArrayList<>();

    public ModelNode(NodeModel rootNodeModel) {
        this.name = rootNodeModel.getName();
        this.transform = DataUtils.convert(rootNodeModel.getTranslation());

        for (NodeModel child : rootNodeModel.getChildren()) {
            children.add(new ModelNode(child));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
