package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.model.material.*;

public record MaterialReference(
    String parent,
    String shader,
    CullType cull,
    BlendType blend,
    IMaterialImages images,
    IMaterialValues values
) implements IMaterialReference {
}
