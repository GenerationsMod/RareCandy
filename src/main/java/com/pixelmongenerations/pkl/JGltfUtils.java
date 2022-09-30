package com.pixelmongenerations.pkl;

import de.javagl.jgltf.model.AccessorData;
import org.joml.Vector3f;

public class JGltfUtils {

    public static Vector3f[] getVertices(AccessorData data) {
        var buf = data.createByteBuffer();
        var count = data.getNumElements();
        var vertices = new Vector3f[count];
        assertTrue(data.getNumComponentsPerElement() == 3);
        assertTrue(data.getComponentType() == float.class);

        for (var i = 0; i < count; i++) {
            vertices[i] = new Vector3f(buf.get(), buf.get(), buf.get());
        }

        return vertices;
    }

    public static int[] getIndices(AccessorData data) {
        return null;
    }

    private static void assertTrue(boolean value) {
        if(!value) throw new RuntimeException("Assertion Failed.");
    }
}
