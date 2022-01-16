package cf.hydos.engine.rendering.resources;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public class MeshResource {
    private final int m_vbo;
    private final int m_ibo;
    private final int m_size;
    private int m_refCount;

    public MeshResource(int size) {
        m_vbo = glGenBuffers();
        m_ibo = glGenBuffers();
        this.m_size = size;
        this.m_refCount = 1;
    }

    @Override
    protected void finalize() {
//		glDeleteBuffers(m_vbo);
//		glDeleteBuffers(m_ibo);
    }

    public void AddReference() {
        m_refCount++;
    }

    public boolean RemoveReference() {
        m_refCount--;
        return m_refCount == 0;
    }

    public int GetVbo() {
        return m_vbo;
    }

    public int GetIbo() {
        return m_ibo;
    }

    public int GetSize() {
        return m_size;
    }
}
