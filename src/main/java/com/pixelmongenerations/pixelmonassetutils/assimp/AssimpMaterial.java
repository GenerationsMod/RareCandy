package com.pixelmongenerations.pixelmonassetutils.assimp;

import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMaterialProperty;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * If I remember correctly, this is a port of a C++ api from Assimp. LWJGL can't bind c++ libs, so it is missing.
 */
public class AssimpMaterial {

    public final AIMaterial material;
    public final HashMap<String, AssimpMaterialProperty<?>> properties = new HashMap<>();

    public AssimpMaterial(AIMaterial material) {
        this.material = material;

        for (int i = 0; i < material.mNumProperties(); i++) {
            AIMaterialProperty property = AIMaterialProperty.create(material.mProperties().get(i));

            String name = property.mKey().dataString();
            int rawType = property.mType();
            ByteBuffer data = property.mData();
            int dataLength = property.mDataLength();
            switch (rawType) {

                /* Array of single-precision (32 Bit) floats
                   It is possible to use aiGetMaterialInteger[Array]() (or the C++-API
                   aiMaterial::Get()) to query properties stored in floating-point format.
                   The material system performs the type conversion automatically.
                */
                case 0x1 -> properties.put(name, AssimpMaterialProperty.of(data.getFloat(), name));

                /* Array of double-precision (64 Bit) floats
                   It is possible to use aiGetMaterialInteger[Array]() (or the C++-API
                   aiMaterial::Get()) to query properties stored in floating-point format.
                   The material system performs the type conversion automatically.
                */
                case 0x2 -> properties.put(name, AssimpMaterialProperty.of(data.getDouble(), name));

                /* The material property is an aiString.
                   Arrays of strings aren't possible, aiGetMaterialString() (or the
                   C++-API aiMaterial::Get()) *must* be used to query a string property.
                */
                case 0x3 -> properties.put(name, AssimpMaterialProperty.of(MemoryUtil.memUTF8(data), name));

                /* Array of (32 Bit) integers
                  It is possible to use aiGetMaterialFloat[Array]() (or the C++-API
                  aiMaterial::Get()) to query properties stored in integer format.
                  The material system performs the type conversion automatically.
                */
                case 0x4 -> {
                    int intDataLength = dataLength / 4;
                    int[] intArray = new int[intDataLength];
                    for (int i1 = 0; i1 < intDataLength; i1++) {
                        intArray[i1] = data.getInt();
                    }
                    properties.put(name, AssimpMaterialProperty.of(intArray, name));
                }

                /* Simple binary buffer, content undefined. Not convertible to anything.
                 */
                case 0x5 -> properties.put(name, AssimpMaterialProperty.of(data, name));

                /*
                 Backup in case all the above fails
                */
                default -> throw new RuntimeException("Property '" + name + "' has unknown data type: " + rawType);
            }
        }
    }

    public String getStringProperty(String s) {
        AssimpMaterialProperty<String> property = (AssimpMaterialProperty<String>) properties.get(s);
        if (property != null) {
            return property.value;
        } else {
            return null;
        }
    }

    public static class AssimpMaterialProperty<T> {
        public String name;
        public T value;

        public static <T> AssimpMaterialProperty<T> of(T value, String key) {
            AssimpMaterialProperty<T> property = new AssimpMaterialProperty<>();
            property.name = key;
            property.value = value;
            return property;
        }
    }
}
