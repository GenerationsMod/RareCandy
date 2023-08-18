package gg.generations.rarecandy.arceus.model.lowlevel;

import static org.lwjgl.opengl.GL11C.*;

public enum IndexType {
    INT(GL_INT),
    UNSIGNED(GL_UNSIGNED_INT),
    SHORT(GL_SHORT),
    UNSIGNED_SHORT(GL_SHORT);

    public final int glType;

    IndexType(int glType) {
        this.glType = glType;
    }
}
