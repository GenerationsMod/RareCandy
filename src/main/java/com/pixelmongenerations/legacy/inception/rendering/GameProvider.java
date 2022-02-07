package com.pixelmongenerations.legacy.inception.rendering;

import org.joml.Matrix4f;

/**
 * Interface to allow proving for example, Minecraft's variables to Inception's Rendering engine.
 */
public interface GameProvider {

    Matrix4f getProjectionMatrix();
    Matrix4f getViewMatrix();
}
