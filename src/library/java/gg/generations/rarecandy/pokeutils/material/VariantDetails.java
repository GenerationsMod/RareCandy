package gg.generations.rarecandy.pokeutils.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.codec.JomlCodecs;
import gg.generations.rarecandy.pokeutils.util.Codecs;
import gg.generations.rarecandy.renderer.animation.Transform;

import java.util.Optional;
import java.util.function.Function;

public record VariantDetails(String material, Boolean hide, Transform transform) {
    public static final Codec<VariantDetails> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.nullable(Codec.STRING, "material", VariantDetails::material),
            Codecs.nullable(Codec.BOOL, "hide", VariantDetails::hide),
            Codecs.nullable(JomlCodecs.TRANSFORM, "transform", new Function<VariantDetails, Transform>() {
                @Override
                public Transform apply(VariantDetails variantDetails) {
                    return variantDetails.transform();
                }
            })
    ).apply(instance, (Optional<String> material, Optional<Boolean> hide, Optional<Transform> offset) -> new VariantDetails(material.orElse(null), hide.orElse(null), offset.orElse(null))));

    public VariantDetails fillIn(VariantDetails filler) {
        var newMaterial = material;
        var newHide = hide;
        var newOffset = transform;

        if (newMaterial == null) newMaterial = filler.material;
        if (newHide == null) newHide = filler.hide != null ? filler.hide : false;
        if (newOffset == null) newOffset = filler.transform != null ? filler.transform : null;

        return new VariantDetails(newMaterial, newHide, newOffset);
    }

    public VariantDetails fillIn() {
        return fillIn(this);
    }
}

