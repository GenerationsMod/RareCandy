package cf.hydos.engine.rendering.shader;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20C;

import java.nio.FloatBuffer;

public class Uniform {

    public final int type;
    public final int count;
    private final int[] uniformLocations;

    public Uniform(int program, String name, int type, int count) {
        this.type = type;
        this.count = count;
        this.uniformLocations = new int[count];

        if(count > 1) {
            for (int i = 0; i < count; i++) {
                uniformLocations[i] = GL20C.glGetUniformLocation(program, name + "[" + i + "]");
            }
        } else {
            uniformLocations[0] = GL20C.glGetUniformLocation(program, name);
        }
    }

    public void uploadMat4f(Matrix4f value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        GL20C.glUniformMatrix4fv(getLocation(), false, buffer);
    }

    public void uploadMat4fs(Matrix4f[] matrix4fs) {
        for (int i = 0; i < matrix4fs.length; i++) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix4fs[i].get(buffer);

            GL20C.glUniformMatrix4fv(getArrayLocation(i), false, buffer);
        }
    }

    public void uploadInt(int value) {
        GL20C.glUniform1i(getLocation(), value);
    }

    private int getArrayLocation(int offset) {
        if(offset > uniformLocations.length) {
            throw new RuntimeException("Tried to get a uniform location for a place outside of the array. Array length is " + uniformLocations.length + ", But we got " + offset);
        }

        return uniformLocations[offset];
    }

    private int getLocation() {
        if (uniformLocations.length > 1) {
            throw new RuntimeException("Tried to get single uniform location when the Uniform is an array? This is most likely an error.");
        }

        return uniformLocations[0];
    }
}
