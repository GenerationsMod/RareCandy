package com.pixelmongenerations.pixelmonassetutils;

import org.lwjgl.opengl.GL11C;

public enum GlFeature {
    DEPTH(GL11C.GL_DEPTH), BLEND(GL11C.GL_BLEND);

    public final int glType;

    GlFeature(int glType) {
        this.glType = glType;
    }
}
