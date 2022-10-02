package me.hydos.gogoat;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GLModel {
    public List<MeshDrawCommand> meshDrawCommands = new ArrayList<>();

    public void runDrawCalls() {
        for (var drawCommand : meshDrawCommands) {
            GL30.glBindVertexArray(drawCommand.vao());
            GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, drawCommand.ebo());
            GL11.glDrawElements(drawCommand.mode(), drawCommand.indexCount(), drawCommand.type(), 0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var glModel = (GLModel) o;
        return Objects.equals(meshDrawCommands, glModel.meshDrawCommands);
    }

    @Override
    public int hashCode() {
        return meshDrawCommands != null ? meshDrawCommands.hashCode() : 0;
    }
}
