//package gg.generations.rarecandy.pokeutils;
//
//import de.javagl.jgltf.model.BufferViewModel;
//import org.joml.Matrix4f;
//import org.joml.Matrix4fc;
//import org.lwjgl.opengl.GL15;
//import org.lwjgl.system.MemoryUtil;
//
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//import java.util.IdentityHashMap;
//import java.util.Map;
//
//public class DataUtils {
//    private static final Map<BufferViewModel, Integer> BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW = new IdentityHashMap<>();
//
//    public static Matrix4fc convert(float[] translation, float[] rotation, float[] scale) {
//        Matrix4f transformMatrix = new Matrix4f().identity();
//        if (translation != null) transformMatrix.translate(translation[0], translation[1], translation[2]);
//        if (rotation != null) transformMatrix.rotate(rotation[0], rotation[1], rotation[2], rotation[3]);
//        if (scale != null) transformMatrix.scale(scale[0], scale[1], scale[2]);
//
//        return transformMatrix;
//    }
//
//    public static void bindArrayBuffer(ByteBuffer bufferViewModel) {
////        var glBufferView = BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW.get(bufferViewModel);
//
////        if (glBufferView == null) {
//            var glBufferView = GL15.glGenBuffers();
//            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
//            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, makeDirect(bufferViewModel), GL15.GL_STATIC_DRAW);
////            BUFFER_VIEW_MODEL_TO_GL_BUFFER_VIEW.put(bufferViewModel, glBufferView);
////        } else {
////            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
////        }
//    }
//
//    public static void bindArrayBuffer(float[] buffer) {
//        var glBufferView = GL15.glGenBuffers();
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
//        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
//    }
//
//    public static void bindArrayBuffer(int[] buffer) {
//        var glBufferView = GL15.glGenBuffers();
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
//        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
//    }
//
//    public static ByteBuffer makeDirect(ByteBuffer javaBuffer) {
//        var directBuffer = MemoryUtil.memAlloc(javaBuffer.capacity());
//
//        for (var i = 0; i < javaBuffer.capacity(); i++) {
//            directBuffer.put(javaBuffer.get());
//        }
//
//        return directBuffer.flip();
//    }
//
//    public static ByteBuffer makeDirect(int[] javaBuffer) {
//        var directBuffer = MemoryUtil.memAlloc(javaBuffer.length);
//
//        Arrays.stream(javaBuffer).forEach(directBuffer::putInt);
//
//        return directBuffer.flip();
//    }
//}
