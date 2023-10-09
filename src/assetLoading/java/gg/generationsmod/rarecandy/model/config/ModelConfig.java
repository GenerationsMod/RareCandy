package gg.generationsmod.rarecandy.model.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.generationsmod.rarecandy.model.config.animation.AnimationInfo;
import gg.generationsmod.rarecandy.model.config.variant.VariantModifier;

import java.util.List;
import java.util.Map;

public class ModelConfig {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(VariantModifier.class, new VariantModifier.Adapter())
            .create();
    public String shaderType;
    public String modelLocation;
    public List<String> hiddenMeshes;
    public Map<String, Map<Integer, String>> materials;
    public Map<String, List<VariantModifier>> variants;
    public Map<String, AnimationInfo> animations;
    public PokemonConfig pokemonConfig;
}
