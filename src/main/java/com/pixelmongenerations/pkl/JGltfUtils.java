package com.pixelmongenerations.pkl;

import com.pixelmongenerations.rarecandy.rendering.Bone;
import de.javagl.jgltf.model.AccessorModel;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class JGltfUtils {

    public static Bone[] readBones(AccessorModel model) {
        var data = model.getAccessorData();
        return null;
    }

    public static Vector3f[] readFloat3(AccessorModel model) {
        var data = model.getAccessorData();
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

    public static Vector2f[] readFloat2(AccessorModel model) {
        var data = model.getAccessorData();
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

    public static int[] readShort1(AccessorModel model) {
        var data = model.getAccessorData();
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

    /**
     * <a href="http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-13-normal-mapping/#computing-the-tangents-and-bitangents">Based off of this</a>
     */
    public static Vector3f[] computeTangents(int[] indices, Vector3f[] vertices, Vector2f[] uvs) {
        var tangents = new Vector3f[vertices.length];
        for (var i = 0; i < tangents.length; i+=3) {
            var indexOffset0 = indices[i + 0];
            var indexOffset1 = indices[i + 1];
            var indexOffset2 = indices[i + 2];

            var v0 = vertices[indexOffset0];
            var v1 = vertices[indexOffset1];
            var v2 = vertices[indexOffset2];

            var uv0 = uvs[indexOffset0];
            var uv1 = uvs[indexOffset1];
            var uv2 = uvs[indexOffset2];

            var deltaPos1 = v1.sub(v0);
            var deltaPos2 = v2.sub(v0);

            var deltaUV1 = uv1.sub(uv0);
            var deltaUV2 = uv2.sub(uv0);

            float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
            var tangent = deltaPos1.mul(deltaUV2.y).sub(deltaPos2.mul(deltaUV1.y)).mul(r);
            tangents[i] = tangent;
        }

        return tangents;
    }
}
