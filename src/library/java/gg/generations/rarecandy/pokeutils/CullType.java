package gg.generations.rarecandy.pokeutils;

import org.lwjgl.opengl.GL11;

public enum CullType {
    Back(GL11.GL_BACK),
    Forward(GL11.GL_BACK),
    None(-1);

    private final int glConstant;

    private static boolean wasCullingEnabled;
    private static int previousCullFace;

    CullType(int glConstant) {
        this.glConstant = glConstant;
    }

    public int getGlConstant() {
        return glConstant;
    }

    public static CullType from(String cull) {
        if (cull == null) return None;
        return switch (cull.toLowerCase()) {
            case "back" -> Back;
            case "forward" -> Forward;
            default -> None;
        };
    }

    public void enable() {
        if (glConstant != -1) {
            // Save previous state
            wasCullingEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            if (wasCullingEnabled) {
                previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
            }

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(glConstant);
        }
    }

    public void disable() {
        if (glConstant != -1) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    public static void restorePreviousState() {
        if (wasCullingEnabled) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(previousCullFace);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }
}