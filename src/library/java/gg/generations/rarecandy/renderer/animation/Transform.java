package gg.generations.rarecandy.renderer.animation;

import org.joml.Vector2f;

public record Transform(Vector2f scale, Vector2f offset) {
    public Transform() {
        this(new Vector2f());
    }

    public Transform(Vector2f offset) {
        this(new Vector2f(1f, 1f), offset);
    }
}





















