package gg.generations.rarecandy.pokeutils;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public record SkeletalTransform(Vector3f position, Quaternionf rotation) {
    public SkeletalTransform() {
        this(new Vector3f(), new Quaternionf());
    }

    public SkeletalTransform scale(float scale) {
        position().div(scale);
        return this;
    }
}
