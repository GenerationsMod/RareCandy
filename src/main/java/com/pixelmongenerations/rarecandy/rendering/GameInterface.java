package com.pixelmongenerations.rarecandy.rendering;

import org.joml.Matrix4f;

/**
 * Interface to allow proving for example, Minecraft's variables to RareCandy's Rendering engine.
 */
public interface GameInterface {

    Matrix4f getProjectionMatrix();
}
