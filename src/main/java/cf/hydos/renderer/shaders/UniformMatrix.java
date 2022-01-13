package cf.hydos.renderer.shaders;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class UniformMatrix extends Uniform {

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public UniformMatrix(String name) {
        super(name);
    }

    public void loadMatrix(Matrix4f matrix) {
        matrix.get(matrixBuffer);
        GL20.glUniformMatrix4fv(super.getLocation(), false, matrixBuffer);
    }
}
