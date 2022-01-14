package cf.hydos.renderer.openglObjects;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GlBuffer {

    private final int bufferId;
    private final int type;

    public GlBuffer(int type) {
        this.bufferId = GL15.glGenBuffers();
        this.type = type;
    }

    public void bind() {
        GL15.glBindBuffer(type, bufferId);
    }

    public void unbind() {
        GL15.glBindBuffer(type, 0);
    }

    public void upload(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        upload(buffer);
    }

    public void upload(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        upload(buffer);
    }

    public void upload(IntBuffer data) {
        GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
    }

    public void upload(FloatBuffer data) {
        GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
    }

    public void delete() {
        GL15.glDeleteBuffers(bufferId);
    }

}
