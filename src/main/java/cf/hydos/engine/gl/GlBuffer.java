package cf.hydos.engine.gl;

import org.lwjgl.opengl.GL45C;

public class GlBuffer {

    public final int id;

    public GlBuffer() {
        this.id = GL45C.glCreateBuffers();
    }

    public void destroy() {

    }
}
