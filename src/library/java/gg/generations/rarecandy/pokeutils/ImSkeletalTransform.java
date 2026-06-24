package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.util.JomlConstants;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class ImSkeletalTransform implements ISkeletalTransform, ImRenderable {
    private final Vector3f position;
    private final Quaternionf rotation;

    static ISkeletalTransform empty() {
        return ImModelConfig.FACTORY.createSkeletalTransform(JomlConstants.VECTOR3F_ZERO, JomlConstants.QUATERIONF_ZERO);
    }

    ImSkeletalTransform(Vector3f position, Quaternionf rotation) {
        this.position = ImGuiConfigUtil.mutable(position, JomlConstants.VECTOR3F_ZERO);
        this.rotation = ImGuiConfigUtil.mutable(rotation, JomlConstants.QUATERIONF_ZERO);
    }

    @Override
    public boolean render() {
        var dirty = false;
        dirty |= ImGuiConfigUtil.inputVector3("Position", position);
        dirty |= ImGuiConfigUtil.inputQuaternion("Rotation", rotation);
        return dirty;
    }

    @Override public Vector3f position() { return position; }
    @Override public Quaternionf rotation() { return rotation; }
}
