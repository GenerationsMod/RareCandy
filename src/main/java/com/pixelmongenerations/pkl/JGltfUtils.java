package com.pixelmongenerations.pkl;

import de.javagl.jgltf.model.AccessorData;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class JGltfUtils {

    public static Vector3f[] readFloat3(AccessorData data) {
        var buf = data.createByteBuffer().asFloatBuffer();
        var count = data.getNumElements();
        var float3Data = new Vector3f[count];
        assertTrue(data.getNumComponentsPerElement() == 3);
        assertTrue(data.getComponentType() == float.class);

        for (var i = 0; i < count; i++) {
            float3Data[i] = new Vector3f(buf.get(), buf.get(), buf.get());
        }

        return float3Data;
    }

    public static Vector2f[] readFloat2(AccessorData data) {
        var buf = data.createByteBuffer().asFloatBuffer();
        var count = data.getNumElements();
        var float2Data = new Vector2f[count];
        assertTrue(data.getNumComponentsPerElement() == 2);
        assertTrue(data.getComponentType() == float.class);

        for (var i = 0; i < count; i++) {
            float2Data[i] = new Vector2f(buf.get(), buf.get());
        }

        return float2Data;
    }

    public static int[] readShort1(AccessorData data) {
        var buf = data.createByteBuffer().asShortBuffer();
        var count = data.getNumElements();
        var short1Data = new int[count];
        assertTrue(data.getNumComponentsPerElement() == 1);
        assertTrue(data.getComponentType() == short.class);

        for (var i = 0; i < count; i++) {
            short1Data[i] = buf.get();
        }

        return short1Data;
    }

    private static void assertTrue(boolean value) {
        if(!value) throw new RuntimeException("Assertion Failed.");
    }
}
