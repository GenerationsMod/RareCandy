package gg.generations.rarecandy.loading.gltf;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.legacy.model.misc.Material;

import java.util.List;
import java.util.Map;

public record VariantModel(List<ModelSet> modelSets) {

    public VariantInstance generateInstance() {
        return new VariantInstance(modelSets);
    }

    public record ModelSet(Model model, Map<String, Material> materialMap, Map<String, Boolean> visibilityMap) {}
};
