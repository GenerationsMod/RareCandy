package gg.generations.rarecandy.pokeutils.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

//TODO: make room for future options for modifying meshes.
public record MeshOptions(boolean invert, List<String> aliases) {
    private static final Codec<MeshOptions> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(MeshOptions::invert),
            Codec.STRING.listOf().optionalFieldOf("aliases", List.of()).forGetter(MeshOptions::aliases)
    ).apply(instance, MeshOptions::new));
}

