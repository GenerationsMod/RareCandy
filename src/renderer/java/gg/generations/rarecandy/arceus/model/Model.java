package gg.generations.rarecandy.arceus.model;

import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

import java.util.function.Function;

public record Model(
        RenderData data,
        Function<String, ShaderProgram> program
) {
}
