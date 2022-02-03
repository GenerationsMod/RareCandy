package cf.hydos.engine.rendering.resources;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public class ObjectBuffers {
    public final int vertexBuffer;
    public final int indexBuffer;
    public final int size;

    public ObjectBuffers(int size) {
        vertexBuffer = glGenBuffers();
        indexBuffer = glGenBuffers();
        this.size = size;
    }
}
