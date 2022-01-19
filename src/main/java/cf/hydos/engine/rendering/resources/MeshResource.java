package cf.hydos.engine.rendering.resources;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public class MeshResource {
    private final int vbo;
    private final int ibo;
    private final int size;

    public MeshResource(int size) {
        vbo = glGenBuffers();
        ibo = glGenBuffers();
        this.size = size;
    }

    @Override
    protected void finalize() {
    }

    public int GetVbo() {
        return vbo;
    }

    public int GetIbo() {
        return ibo;
    }

    public int GetSize() {
        return size;
    }
}
