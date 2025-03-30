package gg.generations.rarecandy.pokeutils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.codec.JomlCodecs;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record SkeletalTransform(Vector3f position, Quaternionf rotation) {
    public static final Codec<SkeletalTransform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            JomlCodecs.VECTOR3F.optionalFieldOf("position", new Vector3f()).forGetter(a -> a.position),
            JomlCodecs.QUATERNIONF.optionalFieldOf("rotatation", new Quaternionf()).forGetter(a -> a.rotation)
    ).apply(instance, SkeletalTransform::new));

    public SkeletalTransform() {
        this(new Vector3f(), new Quaternionf());
    }

    public SkeletalTransform scale(float scale) {
        position().div(scale);
        return this;
    }
}
