package gg.generations.rarecandy.arceus.model.lowlevel;

import static org.lwjgl.opengl.GL11C.*;

public enum IndexType {
    UNSIGNED_INT(GL_UNSIGNED_INT),
    UNSIGNED_SHORT(GL_UNSIGNED_SHORT),
    UNSIGNED_BYTE(GL_UNSIGNED_BYTE);

    public final int glType;

    IndexType(int glType) {
        this.glType = glType;
    }
}
