package gg.generations.rarecandy.pokeutils.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

//TODO: make room for future options for modifying meshes.
public record MeshOptions(boolean invert) {
    public static final Codec<MeshOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(MeshOptions::invert)
    ).apply(instance, MeshOptions::new));
}

