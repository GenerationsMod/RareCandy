package gg.generations.pokeutils;

import java.util.Map;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantReference> defaultVariant;
    public Map<String, Map<String, VariantReference>> variants;

    public record MaterialReference(String texture, String type) {
    }
}
