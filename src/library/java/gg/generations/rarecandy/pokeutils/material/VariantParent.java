package gg.generations.rarecandy.pokeutils.material;

import java.util.Map;

public record VariantParent(String inherits, Map<String, VariantDetails> details) {
}
