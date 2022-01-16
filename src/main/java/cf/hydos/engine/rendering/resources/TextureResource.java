package cf.hydos.engine.rendering.resources;

import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;

public class TextureResource {
    private final int m_id;
    private int m_refCount;

    public TextureResource() {
        this.m_id = glGenTextures();
        this.m_refCount = 1;
    }

    @Override
    protected void finalize() {
        glDeleteBuffers(m_id);
    }

    public void AddReference() {
        m_refCount++;
    }

    public boolean RemoveReference() {
        m_refCount--;
        return m_refCount == 0;
    }

    public int GetId() {
        return m_id;
    }
}
