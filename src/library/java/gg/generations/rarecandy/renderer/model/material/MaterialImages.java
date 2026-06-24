package gg.generations.rarecandy.renderer.model.material;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record MaterialImages(
    String diffuse,
    String layer,
    String mask,
    String emission
) implements IMaterialImages {

//    public MaterialImages processWithImageMap(Map<String, String> imageMap) {
//        var out = new MaterialImages();
//
//        out.diffuse = imageMap.getOrDefault(diffuse, diffuse);
//        out.layer = imageMap.getOrDefault(layer, layer);
//        out.mask = imageMap.getOrDefault(mask, mask);
//        out.emission = imageMap.getOrDefault(emission, emission);
//
//        return out;
//    }
}
