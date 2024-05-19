package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.loading.Attribute;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.GL_BYTE;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_INT;
import static org.lwjgl.opengl.GL11C.GL_SHORT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

public class GLModel implements Closeable {
    public List<MeshDrawCommand> meshDrawCommands = new ArrayList<>();

    public Vector3f dimensions = new Vector3f();

    public int vao = -1;
    public int ebo = -1;
    public int vbo = -1;

    public void runDrawCalls() {
        for (var drawCommand : meshDrawCommands) {
            drawCommand.run();
        }
    }

    @Override
    public int hashCode() {
        return meshDrawCommands != null ? meshDrawCommands.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var glModel = (GLModel) o;
        return Objects.equals(meshDrawCommands, glModel.meshDrawCommands);
    }

    @Override
    public void close() {
        if(vao > -1) {
            GL30.glDeleteVertexArrays(vao);
            vao = -1;
        }
        if(ebo > -1) {
            GL30.glDeleteBuffers(ebo);
            ebo = -1;
        }
        if(vbo > -1) {
            GL30.glDeleteBuffers(vbo);
            vbo = -1;
        }

        meshDrawCommands.clear();
    }

}
