package me.hydos.gogoat.util;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.BufferViewModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {
    private static final Map<BufferViewModel, Integer> BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW = new IdentityHashMap<>();
    private static final List<Integer> bufferViews = new ArrayList<>();

    public static void bindArrayBuffer(BufferViewModel bufferViewModel) {
        var glBufferView = BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW.get(bufferViewModel);

        if (glBufferView == null) {
            glBufferView = GL15.glGenBuffers();
            bufferViews.add(glBufferView);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, makeDirect(bufferViewModel.getBufferViewData()), GL15.GL_STATIC_DRAW);
            BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW.put(bufferViewModel, glBufferView);
        } else {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
        }
    }

    public static ByteBuffer makeDirect(ByteBuffer javaBuffer) {
        var directBuffer = MemoryUtil.memAlloc(javaBuffer.capacity());

        for (var i = 0; i < javaBuffer.capacity(); i++) {
            directBuffer.put(javaBuffer.get());
        }

        directBuffer.flip();
        return directBuffer;
    }

    public void close() {
        for (var bufferId : bufferViews) GL15.glDeleteBuffers(bufferId);
    }
}
