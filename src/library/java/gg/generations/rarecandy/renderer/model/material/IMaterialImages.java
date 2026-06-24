package gg.generations.rarecandy.renderer.model.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface IMaterialImages {
    IMaterialImages DEFAULT = new IMaterialImages() {
        @Override public String diffuse() { return null; }
        @Override public String layer() { return null; }
        @Override public String mask() { return null; }
        @Override public String emission() { return null; }
    };

    String diffuse();
    String layer();
    String mask();
    String emission();

    default Stream<String> stream() {
        return Stream.of(diffuse(), emission(), layer(), mask());
    }

    default boolean isNotEmpty() {
        return diffuse() != null || layer() != null || mask() != null || emission() != null;
    }

    default int[] toArray(List<String> imageNames) {
        return new int[] {
                imageNames.indexOf(diffuse()),
                imageNames.indexOf(layer()),
                imageNames.indexOf(mask()),
                imageNames.indexOf(emission())
        };
    }

    default JsonElement serialize() {
        var obj = new JsonObject();
        JsonUtils.putIf(Objects::nonNull, obj, "diffuse", diffuse(), JsonPrimitive::new);
        JsonUtils.putIf(Objects::nonNull, obj, "layer", layer(), JsonPrimitive::new);
        JsonUtils.putIf(Objects::nonNull, obj, "mask", mask(), JsonPrimitive::new);
        JsonUtils.putIf(Objects::nonNull, obj, "emission", emission(), JsonPrimitive::new);
        return obj;
    }
}
