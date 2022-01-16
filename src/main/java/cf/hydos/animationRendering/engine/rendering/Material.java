package cf.hydos.animationRendering.engine.rendering;

import cf.hydos.animationRendering.engine.rendering.resourceManagement.MappedValues;

import java.util.HashMap;

public class Material extends MappedValues {
    private final HashMap<String, Texture> m_textureHashMap;

    public Material(Texture diffuse, float specularIntensity, float specularPower, Texture normal,
                    Texture dispMap, float dispMapScale, float dispMapOffset) {
        super();
        m_textureHashMap = new HashMap<String, Texture>();
        AddTexture("diffuse", diffuse);
        AddFloat("specularIntensity", specularIntensity);
        AddFloat("specularPower", specularPower);
        AddTexture("normalMap", normal);
        AddTexture("dispMap", dispMap);

        float baseBias = dispMapScale / 2.0f;
        AddFloat("dispMapScale", dispMapScale);
        AddFloat("dispMapBias", -baseBias + baseBias * dispMapOffset);
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
