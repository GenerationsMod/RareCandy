package gg.generations.rarecandy.arceus.model.lowlevel;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.*;

/**
 * Contains information about the layout of vertices.
 */
public class VertexData implements Closeable, Bindable {

    private final int vao;

    /**
     * Constructs a new VertexLayout. Internally generates the OpenGL Vertex Attribute Object
     *
     * @param vertexBuffer
     * @param layout       the layout of attributes to use
     */
    public VertexData(ByteBuffer vertexBuffer, List<Attribute> layout) {
        this.vao = glGenVertexArrays();

        System.out.println("VAO: " + vao + " " + glGetError());

        bind();
        var stride = calculateVertexSize(layout);
        var attribPtr = 0;

        // I hate openGL. why cant I keep the vertex data and vertex layout separate :(
        var vbo = glGenBuffers();
        System.out.println("VBo: " + vbo + " " + glGetError());


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
        unbind();
    }

    private int calculateVertexSize(List<Attribute> layout) {
        var size = 0;
        for (var attrib : layout) size += calculateAttributeSize(attrib);
        return size;
    }

    private int calculateAttributeSize(Attribute attrib) {
        return switch (attrib.glType()) {
            case GL_FLOAT, GL_UNSIGNED_INT, GL_INT -> 4;
            case GL_BYTE, GL_UNSIGNED_BYTE -> 1;
            case GL_SHORT, GL_UNSIGNED_SHORT, GL_HALF_FLOAT -> 2;
            default -> throw new IllegalStateException("Unexpected OpenGL Attribute type: " + attrib.glType() + ". If this is wrong, please contact hydos");
        } * attrib.amount();
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vao);
    }

    @Override
    public void bind() {
        glBindVertexArray(vao);
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }
}
