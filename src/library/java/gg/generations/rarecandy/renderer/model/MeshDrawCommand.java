package gg.generations.rarecandy.renderer.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;

public record MeshDrawCommand(int vao, int mode, int type, int ebo, int indexCount) {

    public void run() {
        GL30.glBindVertexArray(vao());
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo());
        GL11.glDrawElements(mode(), indexCount(), type(), 0);
    }
}
