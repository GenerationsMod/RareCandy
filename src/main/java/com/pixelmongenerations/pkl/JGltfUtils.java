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

    public static Vector3f[] computeTangents(int[] indices, Vector3f[] vertices, Vector2f[] texCoords) {
        var tangents = new Vector3f[indices.length / 3];

        for (var i = 0; i < tangents.length; i++) {
            var offset = i * 3;

            var v0 = vertices[offset+0];
            var v1 = vertices[offset+1];
            var v2 = vertices[offset+2];

            var uv0 = texCoords[offset + 0];
            var uv1 = texCoords[offset + 1];
            var uv2 = texCoords[offset + 2];

            var deltaPos1 = v1.sub(v0, new Vector3f());
            var deltaPos2 = v2.sub(v0, new Vector3f());

            var deltaUV1 = uv1.sub(uv0, new Vector2f());
            var deltaUV2 = uv2.sub(uv0, new Vector2f());

            var r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
            tangents[i] = deltaPos1.mul(deltaUV2.y, new Vector3f()).sub(deltaPos2.mul(deltaUV1.y, new Vector3f()).mul(r));
        }

        return tangents;
    }
}
