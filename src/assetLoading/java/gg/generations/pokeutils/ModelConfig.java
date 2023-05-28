package gg.generations.pokeutils;

import de.javagl.jgltf.impl.v1.Material;

import java.util.List;
import java.util.Map;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, String> materials;

    public Map<String, VariantReference> defaultVariant;
    public Map<String, Map<String, VariantReference>> variants;
}
