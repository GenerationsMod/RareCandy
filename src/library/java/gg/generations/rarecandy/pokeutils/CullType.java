package gg.generations.rarecandy.pokeutils;

import org.lwjgl.opengl.GL11;

public enum CullType {
    Back(GL11.GL_BACK),
    Forward(GL11.GL_FRONT),
    None(-1);

    private final int glConstant;

    CullType(int glConstant) {
        this.glConstant = glConstant;
    }

    public int getGlConstant() {
        return glConstant;
    }

    public static CullType from(String cull) {
        try {
            if (cull.equalsIgnoreCase("back")) return Back;
            else if (cull.equalsIgnoreCase("forward")) return Forward;
            else return None;
        } catch (Exception e) {
            return None;
        }
    }

    public void enable() {
        if(glConstant != -1) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(glConstant);
        }
    }

    public void disable() {
        if(glConstant != -1) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }
}
