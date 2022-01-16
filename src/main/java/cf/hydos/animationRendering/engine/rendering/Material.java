package cf.hydos.animationRendering.engine.rendering;

import cf.hydos.animationRendering.engine.rendering.resourceManagement.MappedValues;

import java.util.HashMap;

public class Material extends MappedValues {
    private final HashMap<String, Texture> m_textureHashMap;

    public Material(Texture diffuse) {
        super();
        m_textureHashMap = new HashMap<>();
        AddTexture("diffuse", diffuse);
    }

    public void AddTexture(String name, Texture texture) {
        m_textureHashMap.put(name, texture);
    }

    public Texture GetTexture(String name) {
        Texture result = m_textureHashMap.get(name);
        if (result != null)
            return result;

        return new Texture("test.png");
    }
}
