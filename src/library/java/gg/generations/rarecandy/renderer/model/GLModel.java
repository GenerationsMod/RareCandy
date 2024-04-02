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

    private boolean uploaded = false;

    public int vao = -1;

    public Vector3f dimensions = new Vector3f();
    public int ebo = -1;
    public ByteBuffer vertexBuffer;
    public ByteBuffer indexBuffer;

    public int indexSize;

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
        uploaded = false;
    }
    public void upload() {
        if (uploaded) return;

        vao = generateVao(vertexBuffer, DEFAULT_ATTRIBUTES);
        GL30.glBindVertexArray(vao);

        ebo = GL15.glGenBuffers();
        glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        meshDrawCommands.add(new MeshDrawCommand(vao, GL11.GL_TRIANGLES, GL_UNSIGNED_INT, ebo, indexSize));
        uploaded = true;
    }

    public void removeFromGpu() {
        if(uploaded) {
            close();
        }
    }

    public static int generateVao(ByteBuffer vertexBuffer, List<Attribute> layout) {
        var vao = glGenVertexArrays();

        glBindVertexArray(vao);
        var stride = calculateVertexSize(layout);
        var attribPtr = 0;

        // I hate openGL. why cant I keep the vertex data and vertex layout separate :(
        var vbo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < layout.size(); i++) {
            var attrib = layout.get(i);
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(
                    i,
                    attrib.amount(),
                    attrib.glType(),
                    false,
                    stride,
                    attribPtr
            );
            attribPtr += calculateAttributeSize(attrib);
        }

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return vao;
    }

    public static int calculateVertexSize(List<Attribute> layout) {
        var size = 0;
        for (var attrib : layout) size += calculateAttributeSize(attrib);
        return size;
    }

    public static int calculateAttributeSize(Attribute attrib) {
        return switch (attrib.glType()) {
            case GL_FLOAT, GL_UNSIGNED_INT, GL_INT -> 4;
            case GL_BYTE, GL_UNSIGNED_BYTE -> 1;
            case GL_SHORT, GL_UNSIGNED_SHORT, GL_HALF_FLOAT -> 2;
            default -> throw new IllegalStateException("Unexpected OpenGL Attribute type: " + attrib.glType() + ". If this is wrong, please contact hydos");
        } * attrib.amount();
    }

    private static List<Attribute> DEFAULT_ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );

    public boolean isUploaded() {
        return uploaded;
    }
}
