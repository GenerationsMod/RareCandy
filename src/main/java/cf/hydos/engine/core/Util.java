package cf.hydos.engine.core;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Util {
    public static FloatBuffer CreateFloatBuffer(int size) {
        return BufferUtils.createFloatBuffer(size);
    }

    public static IntBuffer CreateIntBuffer(int size) {
        return BufferUtils.createIntBuffer(size);
    }

    public static ByteBuffer CreateByteBuffer(int size) {
        return BufferUtils.createByteBuffer(size);
    }

    public static FloatBuffer CreateFlippedBuffer(Matrix4f value) {
        FloatBuffer buffer = CreateFloatBuffer(4 * 4);

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                buffer.put(value.Get(i, j));

        buffer.flip();

        return buffer;
    }
}
