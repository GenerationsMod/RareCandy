package cf.hydos.engine.rendering.resources;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public class MeshResource {
    private final int vbo;
    private final int ibo;
    private final int size;
    private int refCount;

    public MeshResource(int size) {
        vbo = glGenBuffers();
        ibo = glGenBuffers();
        this.size = size;
        this.refCount = 1;
    }

    @Override
    protected void finalize() {
//		glDeleteBuffers(vbo);
//		glDeleteBuffers(ibo);
    }

    public void AddReference() {
        refCount++;
    }

    public boolean RemoveReference() {
        refCount--;
        return refCount == 0;
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
