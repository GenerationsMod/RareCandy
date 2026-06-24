package gg.generations.rarecandy.renderer.storage;

import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

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

    public SSBOBuffer(ByteBuffer buffer, int usage) {
        this();
        replaceStorage(buffer, usage);
    }


    public void ensureCapacity(long newCapacity) {
        if (newCapacity <= 0) return;

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
        long written = pos - pointer;
        if (pointer == 0 || written <= 0) return;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        nglBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, written, pointer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void replaceStorage(ByteBuffer data, int usage) {
        if (pointer != 0) {
            MemoryUtil.nmemFree(pointer);
            pointer = 0;
            pos = 0;
        }

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferData(GL_SHADER_STORAGE_BUFFER, data, usage);
        currentCapacity = data.remaining();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void upload(long byteOffset, ByteBuffer data) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferId);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, byteOffset, data);
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
        checkWrite(16);
        transformationMatrix.getToAddress(pos);
        pos += 16;
        return this;
    }

    public SSBOBuffer put(Matrix3f matrix) {
        put(matrix.m00()).put(matrix.m01()).put(matrix.m02()).put(0.0f);
        put(matrix.m10()).put(matrix.m11()).put(matrix.m12()).put(0.0f);
        put(matrix.m20()).put(matrix.m21()).put(matrix.m22()).put(0.0f);
        return this;
    }
    public SSBOBuffer put(Matrix4f transformationMatrix) {
        checkWrite(64);
        transformationMatrix.getToAddress(pos);
        pos += 64;
        return this;
    }

    public SSBOBuffer put(int value) {
        checkWrite(4);
        MemoryUtil.memPutInt(pos, value);
        pos += 4;
        return this;
    }

    public void put(boolean value) {
        put(value ? 1 : 0);
    }

    public SSBOBuffer put(short value) {
        checkWrite(2);
        MemoryUtil.memPutShort(pos, value);
        pos += 2;
        return this;
    }

    public SSBOBuffer put(float value) {
        checkWrite(4);
        MemoryUtil.memPutFloat(pos, value);
        pos += 4;
        return this;
    }

    public SSBOBuffer put(Vector2f vec2) {
        checkWrite(8);
        vec2.getToAddress(pos);
        pos += 8;
        return this;
    }

    public SSBOBuffer put(Vector3f vec3) {
        checkWrite(12);
        vec3.getToAddress(pos);
        pos += 12;
        return this;
    }

    public SSBOBuffer put(Vector4f vec4) {
        checkWrite(16);
        vec4.getToAddress(pos);
        pos += 16;
        return this;
    }

    public int getBufferId() {
        return bufferId;
    }

    public long capacity() {
        return currentCapacity;
    }

    public void reset() {
        this.pos = pointer;
    }

    public void move(int i) {
        checkWrite(i);
        pos += i;
    }

    protected void checkWrite(long bytes) {
        if (pointer == 0) {
            throw new IllegalStateException("SSBOBuffer has no native storage.");
        }

        long written = pos - pointer;
        if (written + bytes > currentCapacity) {
            throw new IllegalStateException(
                    "SSBOBuffer overflow: writing " + bytes + " bytes at " + written +
                            " with capacity " + currentCapacity
            );
        }
    }
}
