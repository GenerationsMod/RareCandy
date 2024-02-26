package gg.generationsmod.rarecandy.model;

import gg.generationsmod.rarecandy.model.animation.Animation;
import gg.generationsmod.rarecandy.model.animation.Skeleton;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;

import java.util.Map;

public record RawModel(
        String[] materialReferences,
        Mesh[] meshes,
        Skeleton skeleton,
        ModelConfig config,
        Map<String, String> images, Map<String, Animation<?>> animations) {}
