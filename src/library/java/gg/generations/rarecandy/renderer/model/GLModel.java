package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.loading.Attribute;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;

import static gg.generations.rarecandy.renderer.loading.ModelLoader.generateVao;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

/**
 *
 */
public class GLModel implements RenderModel {
    public List<MeshDrawCommand> meshDrawCommands = new ArrayList<>();

    private Vector3f dimensions = new Vector3f();

    public int vao = -1;
    public int ebo = -1;
    public int vbo = -1;

    public void runDrawCalls() {
        for (var drawCommand : meshDrawCommands) {
            drawCommand.run();
        }
    }

    public GLModel(ByteBuffer vertexBuffer, ByteBuffer indexBuffer, int indexSize, int gltType, List<Attribute> attributes) {
        generateVao(this, vertexBuffer, attributes);
            GL30.glBindVertexArray(vao);
            ebo = GL15.glGenBuffers();
            glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            meshDrawCommands.add(new MeshDrawCommand(vao, GL11.GL_TRIANGLES, gltType, ebo, indexSize));
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(indexBuffer);
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
        if (vao > -1) {
            GL30.glDeleteVertexArrays(vao);
            vao = -1;
        }
        if (ebo > -1) {
            GL30.glDeleteBuffers(ebo);
            ebo = -1;
        }
        if (vbo > -1) {
            GL30.glDeleteBuffers(vbo);
            vbo = -1;
        }

        meshDrawCommands.clear();
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

//    private Map<Material, List<Consumer<Pipeline>>> EMPTY = Collections.emptyMap();

}