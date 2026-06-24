package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.util.JomlConstants;
import gg.generations.rarecandy.renderer.animation.ITransform;
import org.joml.Vector2f;

final class ImTransform implements ITransform, ImRenderable {
    private final Vector2f scale;
    private final Vector2f offset;

    ImTransform(Vector2f scale, Vector2f offset) {
        this.scale = ImGuiConfigUtil.mutable(scale, JomlConstants.VECTOR2F_ONE);
        this.offset = ImGuiConfigUtil.mutable(offset, JomlConstants.VECTOR2F_ZERO);
    }

    @Override
    public boolean render() {
        var dirty = false;
        dirty |= ImGuiConfigUtil.inputVector2("Scale", scale);
        dirty |= ImGuiConfigUtil.inputVector2("Offset", offset);
        return dirty;
    }

    @Override public Vector2f scale() { return scale; }
    @Override public Vector2f offset() { return offset; }
}
