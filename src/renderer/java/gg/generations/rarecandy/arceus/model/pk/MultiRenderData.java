package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.model.lowlevel.Bindable;
import gg.generations.rarecandy.arceus.model.lowlevel.DrawMode;
import gg.generations.rarecandy.arceus.model.lowlevel.IndexType;
import gg.generations.rarecandy.arceus.model.lowlevel.VertexData;
import org.lwjgl.opengl.GL15C;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;

/**
 * Data that can be directly uploaded to the GPU.
 */
public class MultiRenderData implements Bindable, Closeable, Renderable {

    public final DrawMode mode;
    public final VertexData vertexData;
    private final int ebo;
    public final IndexType indexType;
    public final List<MultiModel.MeshDetail> details;

    /**
     * The {@link ByteBuffer} used must be allocated either through {@link org.lwjgl.BufferUtils#createByteBuffer(int)} or {@link org.lwjgl.system.MemoryUtil#memAlloc(int)}
     */
    public MultiRenderData(DrawMode mode, VertexData vertexData, ByteBuffer indices, IndexType type, ArrayList<MultiModel.MeshDetail> details) {
        // Generate Index Buffer Data
        this.ebo = glGenBuffers();

        glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        this.indexType = type;
        this.details = details;

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
        vertexData.close();
        glDeleteBuffers(ebo);
    }

    @Override
    public void render(int index) {
        var detail = details.get(index);

        glDrawElements(this.mode.glType, detail.count(), this.indexType.glType, detail.offset());
    }
}
