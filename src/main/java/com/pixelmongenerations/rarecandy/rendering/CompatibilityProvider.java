package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

/**
 * Interface to allow proving for example, Minecraft's variables to Inception's Rendering engine.
 */
public interface CompatibilityProvider {

    Matrix4f getProjectionMatrix();
}
