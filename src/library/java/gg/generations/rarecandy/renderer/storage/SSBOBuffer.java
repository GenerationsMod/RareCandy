package gg.generations.rarecandy.renderer.storage;

import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL43.*;

/**
 * Manages a single SSBO with dynamic size control
 */
public class SSBOBuffer {
    private int bufferId;
    private long currentCapacity; // in bytes
    private long pointer; // native memory buffer
    private long pos;

    public SSBOBuffer() {
        this.bufferId = glGenBuffers();
        this.currentCapacity = 0;
        this.pos = 0;
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
            pos = pointer;
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

        this.pos = 0;
    }

    public SSBOBuffer put(Matrix2f transformationMatrix) {
        transformationMatrix.getToAddress(pos);
        pos += 16;
        return this;
    }

    public SSBOBuffer put(Matrix3f transformationMatrix) {
        transformationMatrix.getToAddress(pos);
        pos += 36;
        return this;
    }
    public SSBOBuffer put(Matrix4f transformationMatrix) {
        transformationMatrix.getToAddress(pos);
        pos += 64;
        return this;
    }

    public SSBOBuffer put(int value) {
        MemoryUtil.memPutInt(pos, value);
        pos += 4;
        return this;
    }

    public void put(boolean value) {
        put(value ? 1 : 0);
    }

    public SSBOBuffer put(short value) {
        MemoryUtil.memPutShort(pos, value);
        pos += 2;
        return this;
    }

    public SSBOBuffer put(float value) {
        MemoryUtil.memPutFloat(pos, value);
        pos += 4;
        return this;
    }

    public SSBOBuffer put(Vector2f vec2) {
        vec2.getToAddress(pos);
        pos += 8;
        return this;
    }

    public SSBOBuffer put(Vector3f vec3) {
        vec3.getToAddress(pos);
        pos += 12;
        return this;
    }

    public SSBOBuffer put(Vector4f vec4) {
        vec4.getToAddress(pos);
        pos += 16;
        return this;
    }

    public int getBufferId() {
        return bufferId;
    }

    public void reset() {
        this.pos = pointer;
    }
}