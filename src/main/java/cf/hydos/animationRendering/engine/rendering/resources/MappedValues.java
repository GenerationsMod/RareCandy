package cf.hydos.animationRendering.engine.rendering.resources;

import cf.hydos.animationRendering.engine.core.Vector3f;

import java.util.HashMap;

public abstract class MappedValues {
    private final HashMap<String, Vector3f> m_vector3fHashMap;
    private final HashMap<String, Float> m_floatHashMap;

    public MappedValues() {
        m_vector3fHashMap = new HashMap<String, Vector3f>();
        m_floatHashMap = new HashMap<String, Float>();
    }

    public void AddVector3f(String name, Vector3f vector3f) {
        m_vector3fHashMap.put(name, vector3f);
    }

    public void AddFloat(String name, float floatValue) {
        m_floatHashMap.put(name, floatValue);
    }

    public Vector3f GetVector3f(String name) {
        Vector3f result = m_vector3fHashMap.get(name);
        if (result != null)
            return result;

        return new Vector3f(0, 0, 0);
    }

    public float GetFloat(String name) {
        Float result = m_floatHashMap.get(name);
        if (result != null)
            return result;

        return 0;
    }
}
