package gg.generations.rarecandy.renderer.components;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;

public record DrawRecord(int base, int size) {
    public void render() {
        GL43C.glDrawArrays(GL11C.GL_TRIANGLES, base, size);
    }
}
