package gg.generations.rarecandy.renderer.storage;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;

public class DrawBuffer extends SSBOBuffer {
    private int drawAmount;

    public DrawBuffer(int size) {
        super(size);
        this.drawAmount = 0;
    }

    @Override
    public void reset() {
        super.reset();
        this.drawAmount = 0;
    }

    public void putDraw(int count, int instanceCount, int first, int baseInstance) {
        put(count).put(instanceCount).put(first).put(baseInstance);
        drawAmount++;
    }

    public void render() {
        if (drawAmount == 0) return;

        GL43C.glBindBuffer(GL43C.GL_DRAW_INDIRECT_BUFFER, getBufferId());

        GL43C.glMultiDrawArraysIndirect(
                GL11C.GL_TRIANGLES,
                0L,
                drawAmount,
                16
        );
    }
}
