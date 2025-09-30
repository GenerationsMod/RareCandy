package gg.generations.rarecandy.renderer.pipeline.util;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.lwjgl.opengl.GL20C;

/**
 * Context object passed to uniform upload callbacks.
 * Provides access to the current GL program, instance, and render object.
 */
public record UniformUploadContext(int program, ObjectInstance instance, RenderObject object) {

    /**
     * Retrieve raw uniform location from OpenGL.
     * Normally you use the Uniform object directly, but this is available for flexibility.
     */
    public int getUniformLocation(String name) {
        return GL20C.glGetUniformLocation(program, name);
    }

    public Material getMaterial() {
        if(object == null) return null;

        var variant = instance != null ? instance.variant() : null;


        return object.getMaterial(variant);
    }

}
