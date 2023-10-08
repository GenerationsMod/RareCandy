package gg.generations.rarecandy.pokeutils;

import org.lwjgl.opengl.GL11;

public enum BlendType {
    None(-1, -1), Regular(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    private final int src;
    private final int dst;

    BlendType(int src, int dst) {
        this.src = src;
        this.dst = dst;
    }

    public void enable() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(src, dst);
    }

    public void disable() {
        GL11.glDisable(GL11.GL_BLEND);
    }
}
