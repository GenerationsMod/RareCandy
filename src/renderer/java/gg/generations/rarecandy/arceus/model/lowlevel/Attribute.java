package gg.generations.rarecandy.arceus.model.lowlevel;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_HALF_FLOAT;

/**
 * An attribute of a vertex. Recommended ones are bundled inside RareCandy.
 * @param glType the GL Scalar type you are using
 * @param amount the amount of the type you have in each attribute
 */
public record Attribute(int glType, int amount) {
    public static final Attribute POSITION = new Attribute(GL_FLOAT, 3);
    public static final Attribute NORMAL = new Attribute(GL_FLOAT, 3);
    public static final Attribute COLOR = new Attribute(GL_UNSIGNED_BYTE, 4);
    public static final Attribute TEXCOORD = new Attribute(GL_FLOAT, 2);
    public static final Attribute BONE_IDS = new Attribute(GL_UNSIGNED_BYTE, 4);
    public static final Attribute BONE_WEIGHTS = new Attribute(GL_FLOAT, 4);
}
