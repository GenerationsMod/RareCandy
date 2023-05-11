package gg.generations.pokeutils;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public final ModelNode parent;
    public int id = -1;
    public final Matrix4f transform;
    public final List<ModelNode> children = new ArrayList<>();

    public ModelNode(NodeModel rootNodeModel, ModelNode parent) {
        this.parent = parent;
        this.name = rootNodeModel.getName().replace(".trmdl", "");
        this.transform = new Matrix4f().add(DataUtils.convert(rootNodeModel.getTranslation(), rootNodeModel.getRotation(), rootNodeModel.getScale()));

        for (var child : rootNodeModel.getChildren()) {
            children.add(new ModelNode(child, this));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
