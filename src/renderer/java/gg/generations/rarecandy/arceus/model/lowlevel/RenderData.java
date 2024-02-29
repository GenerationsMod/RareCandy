package gg.generations.rarecandy.arceus.model.lowlevel;

import gg.generations.rarecandy.arceus.model.pk.Renderable;
import org.lwjgl.opengl.GL15C;

import java.io.Closeable;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15C.*;

/**
 * Data that can be directly uploaded to the GPU.
 */
public class RenderData implements Bindable, Closeable, Renderable {

    public final DrawMode mode;
    public final VertexData vertexData;
    private final int ebo;
    public final IndexType indexType;
    public final int indexCount;

    /**
     * The {@link ByteBuffer} used must be allocated either through {@link org.lwjgl.BufferUtils#createByteBuffer(int)} or {@link org.lwjgl.system.MemoryUtil#memAlloc(int)}
     */
    public RenderData(DrawMode mode, VertexData vertexData, ByteBuffer indices, IndexType type, int indexCount) {
        // Generate Index Buffer Data
        this.ebo = glGenBuffers();

        glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        this.indexType = type;
        this.indexCount = indexCount;

        this.mode = mode;
        this.vertexData = vertexData;
    }

    @Override
    public void bind() {
        glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
    }

    @Override
    public void unbind() {
        glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void close() {
        glDeleteBuffers(ebo);
    }

    @Override
    public void render(int index) {
        glDrawElements(this.mode.glType, this.indexCount, this.indexType.glType, 0);
    }
}
