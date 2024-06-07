package gg.generations.rarecandy.pokeutils;

import de.javagl.jgltf.model.BufferViewModel;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public class DataUtils {
    private static final Map<BufferViewModel, Integer> BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW = new IdentityHashMap<>();
    private static final Map<Integer, Integer> BUFFER_USAGE = new IdentityHashMap<>();

    public static Matrix4fc convert(float[] translation, float[] rotation, float[] scale) {
        Matrix4f transformMatrix = new Matrix4f().identity();
        if (translation != null) transformMatrix.translate(translation[0], translation[1], translation[2]);
        if (rotation != null) transformMatrix.rotate(rotation[0], rotation[1], rotation[2], rotation[3]);
        if (scale != null) transformMatrix.scale(scale[0], scale[1], scale[2]);

        return transformMatrix;
    }

    public static int bindArrayBuffer(BufferViewModel bufferViewModel) {
        var glBufferView = BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW.get(bufferViewModel);

        if (glBufferView == null) {
            glBufferView = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
            var buffer = makeDirect(bufferViewModel.getBufferViewData());
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
            MemoryUtil.memFree(buffer);
            BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW.put(bufferViewModel, glBufferView);
        } else {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
        }
        return glBufferView;
    }

    public static void deleteBuffer(int id) {
        if(BUFFER_USAGE.containsKey(id)) {
            var usage = BUFFER_USAGE.get(id) - 1;

            if (usage >= 1) {
                BUFFER_USAGE.put(id, usage);
                return;
            } else {
                BUFFER_USAGE.remove(id);
            }
        }

        GL15.glDeleteBuffers(id);
    }

    public static ByteBuffer makeDirect(ByteBuffer javaBuffer) {
        var directBuffer = MemoryUtil.memAlloc(javaBuffer.capacity());

        for (var i = 0; i < javaBuffer.capacity(); i++) {
            directBuffer.put(javaBuffer.get());
        }

        return directBuffer.flip();
    }

    public static ByteBuffer makeDirect(int[] javaBuffer) {
        var directBuffer = MemoryUtil.memAlloc(javaBuffer.length);

        Arrays.stream(javaBuffer).forEach(directBuffer::putInt);

        return directBuffer.flip();
    }

    public static void deleteBuffer(int[] ids) {
        Arrays.stream(ids).forEach(DataUtils::deleteBuffer);
    }
}
