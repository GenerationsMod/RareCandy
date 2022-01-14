package cf.hydos.renderer.openglObjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class VertexAttributesObject {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_INT = 4;
    public final int id;
    private final List<GlBuffer> dataGlBuffers = new ArrayList<>();
    private GlBuffer indexBufferObject;
    private int indexCount;

    public VertexAttributesObject() {
        this.id = GL30.glGenVertexArrays();
    }

    public int getIndexCount() {
        return indexCount;
    }

    public void bind(int... attributes) {
        bind();
        for (int i : attributes) {
            GL20.glEnableVertexAttribArray(i);
        }
    }

    public void unbind(int... attributes) {
        for (int i : attributes) {
            GL20.glDisableVertexAttribArray(i);
        }
        unbind();
    }

    public void createIndexBuffer(int[] indices) {
        this.indexBufferObject = new GlBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
        indexBufferObject.bind();
        indexBufferObject.upload(indices);
        this.indexCount = indices.length;
    }

    public void createAttribute(int attribute, float[] data, int attrSize) {
        GlBuffer glBuffer = new GlBuffer(GL15.GL_ARRAY_BUFFER);
        glBuffer.bind();
        glBuffer.upload(data);
        GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
        glBuffer.unbind();
        dataGlBuffers.add(glBuffer);
    }

    public void createIntAttribute(int attribute, int[] data, int attrSize) {
        GlBuffer glBuffer = new GlBuffer(GL15.GL_ARRAY_BUFFER);
        glBuffer.bind();
        glBuffer.upload(data);
        GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
        glBuffer.unbind();
        dataGlBuffers.add(glBuffer);
    }

    public void delete() {
        GL30.glDeleteVertexArrays(id);
        for (GlBuffer glBuffer : dataGlBuffers) {
            glBuffer.delete();
        }
        indexBufferObject.delete();
    }

    private void bind() {
        GL30.glBindVertexArray(id);
    }

    private void unbind() {
        GL30.glBindVertexArray(0);
    }
}
