package gg.generations.rarecandy.pokeutils.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.util.Codecs;

import java.util.Map;

public record VariantParent(String inherits, Map<String, VariantDetails> details) {
    public static final Codec<VariantParent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.nullable(Codec.STRING, "parent", VariantParent::inherits),
            Codecs.nullable(Codecs.map(Codec.STRING, VariantDetails.CODEC), "details", VariantParent::details))
            .apply(instance, (parent, details) -> new VariantParent(parent.orElse(null), details.orElse(null))));
}
