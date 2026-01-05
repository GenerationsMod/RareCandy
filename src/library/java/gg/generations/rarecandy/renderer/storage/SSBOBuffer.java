package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.pipeline.util.SSBOBinding;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL15.*;

/**
 * Manages a single SSBO with dynamic size control
 */
public class SSBOBuffer {
    private int bufferId;
    private long currentCapacity; // in bytes
    private long pointer; // native memory buffer

    public SSBOBuffer() {
        this.bufferId = glGenBuffers();
        this.currentCapacity = 0;
    }

    public SSBOBuffer(int capacity) {
        this();
        ensureCapacity(capacity);
    }

    public void ensureCapacity(long newCapacity) {
        // Resize native buffer if needed
        if (pointer == 0 || currentCapacity < newCapacity) {
            if(pointer > 0) MemoryUtil.nmemFree(pointer);
            pointer = MemoryUtil.nmemAlloc((int) newCapacity);
        }

        // Resize GPU buffer if needed
        if (newCapacity > currentCapacity) {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
            glBufferData(GL_SHADER_STORAGE_BUFFER, newCapacity, GL_DYNAMIC_DRAW);
            currentCapacity = newCapacity;
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }
    }

    public void upload() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        nglBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, currentCapacity, pointer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void delete() {
        if (bufferId != 0) {
            glDeleteBuffers(bufferId);
            bufferId = 0;
            currentCapacity = 0;
        }
        if (pointer != 0) {
            MemoryUtil.nmemFree(pointer);
            pointer = 0;
        }
    }

    public void put(int pos, Matrix4f transformationMatrix) {
        transformationMatrix.getToAddress(pointer + pos);
    }

    public void put(int pos, float value) {
        MemoryUtil.memPutFloat(pointer + pos, value);
    }

    public void put(int pos, Vector2f vec2) {
        vec2.getToAddress(pointer + pos);
    }

    public int getBufferId() {
        return bufferId;
    }
}