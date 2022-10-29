package com.pokemod.rarecandy.pipeline;

import com.pokemod.rarecandy.components.RenderObject;
import com.pokemod.rarecandy.rendering.InstanceState;

public record UniformUploadContext(RenderObject object, InstanceState instance, Uniform uniform) {}
