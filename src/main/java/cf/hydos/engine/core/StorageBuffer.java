package cf.hydos.engine.core;

import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;

public class StorageBuffer {

    public final int id;

    public StorageBuffer(int size) {
        this.id = GL45C.glCreateBuffers();
        GL45C.glNamedBufferData(this.id, size, GL15C.GL_STATIC_DRAW);
    }

    public long map() {
        return GL45C.nglMapNamedBuffer(this.id, GL15C.GL_WRITE_ONLY);
    }

    public void bind(int binding) {
        GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, this.id);
    }
}
