package gg.generations.rarecandy.arceus.model.lowlevel;

import static org.lwjgl.opengl.GL11C.*;

public enum DrawMode {
    TRIANGLES(GL_TRIANGLES),
    QUADS(GL_QUADS),
    TRIANGLE_FAN(GL_TRIANGLE_FAN),
    TRIANGLE_STRIP(GL_TRIANGLE_STRIP),
    WIRE_FRAME(GL_LINE);

    public final int glType;

    DrawMode(int glType) {
        this.glType = glType;
    }
}
