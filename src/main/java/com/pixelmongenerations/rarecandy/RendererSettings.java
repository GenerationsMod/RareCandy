package com.pixelmongenerations.rarecandy;

import org.lwjgl.opengl.GLCapabilities;

public class RendererSettings {

    public final TransparencyMethod transparencyMethod;

    public RendererSettings(GLCapabilities capabilities) {
        transparencyMethod = capabilities.OpenGL43 ? TransparencyMethod.LINKED_LIST : TransparencyMethod.NONE;
    }

    public enum TransparencyMethod {
        LINKED_LIST, NONE
    }
}
