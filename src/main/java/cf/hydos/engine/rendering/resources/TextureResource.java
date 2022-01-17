package cf.hydos.engine.rendering.resources;

import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;

public class TextureResource {
    private final int id;
    private int refCount;

    public TextureResource() {
        this.id = glGenTextures();
        this.refCount = 1;
    }

    @Override
    protected void finalize() {
        glDeleteBuffers(id);
    }

    public void AddReference() {
        refCount++;
    }

    public boolean RemoveReference() {
        refCount--;
        return refCount == 0;
    }

    public int GetId() {
        return id;
    }
}
