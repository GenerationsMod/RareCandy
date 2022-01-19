package cf.hydos.engine.rendering.resources;

import org.joml.Vector3f;

import java.util.HashMap;

public abstract class MappedValues {
    private final HashMap<String, Vector3f> vector3fHashMap;
    private final HashMap<String, Float> floatHashMap;

    public MappedValues() {
        vector3fHashMap = new HashMap<>();
        floatHashMap = new HashMap<>();
    }

    public void AddVector3f(String name, Vector3f vector3f) {
        vector3fHashMap.put(name, vector3f);
    }

    public void AddFloat(String name, float floatValue) {
        floatHashMap.put(name, floatValue);
    }

    public Vector3f GetVector3f(String name) {
        Vector3f result = vector3fHashMap.get(name);
        if (result != null)
            return result;

        return new Vector3f(0, 0, 0);
    }

    public float GetFloat(String name) {
        Float result = floatHashMap.get(name);
        if (result != null)
            return result;

        return 0;
    }
}
