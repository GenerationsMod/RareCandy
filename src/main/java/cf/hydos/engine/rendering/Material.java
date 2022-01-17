package cf.hydos.engine.rendering;

import cf.hydos.engine.rendering.resources.MappedValues;

import java.util.HashMap;

public class Material extends MappedValues {
    private final HashMap<String, Texture> textureHashMap;

    public Material(Texture diffuse) {
        super();
        textureHashMap = new HashMap<>();
        AddTexture("diffuse", diffuse);
    }

    public void AddTexture(String name, Texture texture) {
        textureHashMap.put(name, texture);
    }

    public Texture GetTexture(String name) {
        Texture result = textureHashMap.get(name);
        if (result != null)
            return result;

        return new Texture("test.png");
    }
}
