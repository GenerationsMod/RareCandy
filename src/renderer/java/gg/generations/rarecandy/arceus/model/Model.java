package gg.generations.rarecandy.arceus.model;

import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

public record Model(
        RenderData data,
        ShaderProgram program
) {}
