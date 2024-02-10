package gg.generationsmod.rarecandy.model;

import gg.generationsmod.rarecandy.model.animation.Skeleton;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;

public record Model(
        String[] materialReferences,
        Mesh[] meshes,
        Skeleton skeleton,
        ModelConfig config
) {}