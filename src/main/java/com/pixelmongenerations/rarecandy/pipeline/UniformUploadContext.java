package com.pixelmongenerations.rarecandy.pipeline;

import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;

public record UniformUploadContext(RenderObject object, InstanceState instance, Uniform uniform) {}
