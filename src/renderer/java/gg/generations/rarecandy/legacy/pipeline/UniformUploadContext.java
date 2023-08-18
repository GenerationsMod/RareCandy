package gg.generations.rarecandy.legacy.pipeline;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;

public record UniformUploadContext(
        Model object,
        RenderingInstance instance,
        Uniform uniform
) {}
