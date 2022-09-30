package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

public record InstanceState(Matrix4f transformationMatrix, Matrix4f viewMatrix, String materialId) {}
