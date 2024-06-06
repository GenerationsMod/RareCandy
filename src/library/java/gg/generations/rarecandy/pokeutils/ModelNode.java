package gg.generations.rarecandy.pokeutils;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public final ModelNode parent;
    public final Matrix4f transform;
    public final Vector3f posePosition;
    public final Quaternionf poseRotation;
    public final Vector3f poseScale;

    public final List<ModelNode> children = new ArrayList<>();
    public int id = -1;

    public ModelNode(NodeModel rootNodeModel, ModelNode parent) {
        this.parent = parent;
        this.name = rootNodeModel.getName().replace(".trmdl", "");
        this.transform = new Matrix4f().add(DataUtils.convert(rootNodeModel.getTranslation(), rootNodeModel.getRotation(), rootNodeModel.getScale()));
        this.posePosition = transform.getTranslation(new Vector3f());
        this.poseRotation = transform.getUnnormalizedRotation(new Quaternionf());
        this.poseScale = transform.getScale(new Vector3f());


        for (var child : rootNodeModel.getChildren()) {
            children.add(new ModelNode(child, this));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
