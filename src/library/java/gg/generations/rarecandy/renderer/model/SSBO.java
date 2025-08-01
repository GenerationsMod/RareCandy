package gg.generations.rarecandy.renderer.model;

import org.lwjgl.opengl.GL43C;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL43C.*;

public class SSBO {

    private final int bufferId;
    private final int bindingPoint;
    private final int usage;
    private final int stride;
    private int capacityBytes;

    public SSBO(int bindingPoint, int stride, int initialCapacityBytes, int usage) {
        this.bufferId = glGenBuffers();
        this.bindingPoint = bindingPoint;
        this.stride = stride;
        this.capacityBytes = initialCapacityBytes;
        this.usage = usage;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferData(GL_SHADER_STORAGE_BUFFER, initialCapacityBytes, usage);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, bindingPoint, bufferId);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void upload(ByteBuffer data) {
        ensureCapacity(data.limit());
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void updateSubData(int offsetBytes, ByteBuffer partial) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, offsetBytes, partial);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void mapWrite(Consumer<ByteBuffer> writer) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        ByteBuffer buffer = glMapBufferRange(
                GL_SHADER_STORAGE_BUFFER,
                0,
                capacityBytes,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT | GL_MAP_UNSYNCHRONIZED_BIT
        );
        writer.accept(buffer);
        glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    protected void ensureCapacity(int requiredBytes) {
        if (requiredBytes > capacityBytes) {
            capacityBytes = Math.max(requiredBytes, capacityBytes * 2);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
            glBufferData(GL_SHADER_STORAGE_BUFFER, capacityBytes, usage);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }
    }

    public int getStride() {
        return stride;
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getCapacityBytes() {
        return capacityBytes;
    }

    public void bind() {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, bindingPoint, bufferId);
    }

    public void delete() {
        glDeleteBuffers(bufferId);
    }
}
