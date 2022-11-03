package com.pokemod.pokeutils;

import de.javagl.jgltf.model.NodeModel;
import com.pokemod.rarecandy.DataUtils;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public int id = -1;
    public final Matrix4f transform;
    public final List<ModelNode> children = new ArrayList<>();

    public ModelNode(NodeModel rootNodeModel) {
        this.name = rootNodeModel.getName();
        this.transform = new Matrix4f().add(DataUtils.convert(rootNodeModel.getTranslation(), rootNodeModel.getRotation(), rootNodeModel.getScale()));

        for (NodeModel child : rootNodeModel.getChildren()) {
            children.add(new ModelNode(child));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
