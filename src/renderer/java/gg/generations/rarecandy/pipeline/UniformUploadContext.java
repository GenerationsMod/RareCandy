package gg.generations.rarecandy.pipeline;

import gg.generations.rarecandy.components.RenderObject;
import gg.generations.rarecandy.rendering.ObjectInstance;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {}
