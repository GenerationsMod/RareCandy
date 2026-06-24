package gg.generations.rarecandy.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.IModelConfig;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JsonConfigConvert {
    public static void main(String[] args) throws IOException {
        var p = Path.of("D:\\Git Repos\\Models\\Generation Packs\\Generation 1\\assets\\generations_core\\bedrock\\pokemon\\models");
        Files.walk(p, 1).filter(a -> !a.equals(p)).filter(Files::isDirectory).forEach(new Consumer<Path>() {
            @Override
            public void accept(Path path) {
                JsonConfigConvert.accept(path);
            }
        });
    }

    public static String convert(String bytes) throws IOException {

        var config = IModelConfig.GSON.fromJson(bytes, JsonObject.class);

        var materials = config.getAsJsonObject("materials");

        var effects = new HashSet<String>();
        var effectMap = new HashMap<String, String>();

        materials.asMap().forEach((s, element) -> {
            if (element instanceof JsonObject obj) {
                var effect = obj.has("effect") ? obj.get("effect").getAsString() : "";

                if (!effect.isEmpty()) {
                    effectMap.put(s, effect);
                    effects.add(effect);
                    return;
                }

                if (obj.has("shader") && obj.get("shader").getAsString().equals("solid")) obj.remove("shader");
                if (obj.has("cull") && obj.get("cull").getAsString().equals("None")) obj.remove("cull");
                if (obj.has("blend") && obj.get("blend").getAsString().equals("None")) obj.remove("blend");
                if (obj.has("values")) {
                    if (obj.get("values").getAsJsonObject().isEmpty()) {
                        obj.remove("values");
                    } else {
                        var values = obj.get("values").getAsJsonObject();

                        compactIfColorExists(values, "color");
                        compactIfColorExists(values, "baseColor1");
                        compactIfColorExists(values, "baseColor2");
                        compactIfColorExists(values, "baseColor3");
                        compactIfColorExists(values, "baseColor4");
                        compactIfColorExists(values, "baseColor5");
                        compactIfColorExists(values, "emiColor1");
                        compactIfColorExists(values, "emiColor2");
                        compactIfColorExists(values, "emiColor3");
                        compactIfColorExists(values, "emiColor4");
                        compactIfColorExists(values, "emiColor5");
                        compactIfColorExists(values, "emiIntensity1");
                        compactIfColorExists(values, "emiIntensity2");
                        compactIfColorExists(values, "emiIntensity3");
                        compactIfColorExists(values, "emiIntensity4");
                        compactIfColorExists(values, "emiIntensity5");
                    }


                }
            }
        });

        effectMap.keySet().forEach(materials::remove);

        var variantsObject = config.getAsJsonObject("variants");

        var list = variantsObject.keySet().stream().filter(a -> effects.stream().anyMatch(a::startsWith)).collect(Collectors.toMap(a -> a, a -> effects.stream().filter(b -> a.startsWith(b)).findFirst().get()));

        list.forEach((variant, effect) -> {
            var parent = variant.replace(effect + "_", "");

            var variantJson = variantsObject.getAsJsonObject(variant);
            variantJson.asMap().values().forEach(element -> {
                var obj = element.getAsJsonObject();

                var material = obj.has("material") ? obj.get("material").getAsString() : "";

                if (effectMap.containsKey(material)) {
                    obj.remove("material");
                    obj.addProperty("effect", effect);
                }
            });

            variantJson.addProperty("parent", parent);
        });

        if (config.has("offsets") && config.getAsJsonObject("offsets").isEmpty()) config.remove("offsets");

        return IModelConfig.GSON.toJson(config);

//        Files.writeString(path, gson.toJson(config));
    }

    private static void compactIfColorExists(JsonObject values, String color) {
        if (values.has(color)) {
            var obj = values.get(color);


            if (obj instanceof JsonObject object) {
                var r = object.get("x").getAsFloat();
                var g = object.get("y").getAsFloat();
                var b = object.get("z").getAsFloat();

                var colorInt = new Color(r, g, b).getRGB();

                values.addProperty(color, "#" + Integer.toHexString(colorInt).substring(2).toUpperCase());
            } else if (obj instanceof JsonArray array) {
                var r = array.get(0).getAsFloat();
                var g = array.get(1).getAsFloat();
                var b = array.get(2).getAsFloat();

                var colorInt = new Color(r, g, b).getRGB();

                values.addProperty(color, "#" + Integer.toHexString(colorInt).substring(2).toUpperCase());
            }
        }
    }

    private static void accept(Path x) {

        if (x == null) return;

        try {
            if (!x.toString().endsWith(".pk")) {
                var path = x.resolve("config.json");

                var string = Files.readString(path);

                System.out.println(path);

                Files.writeString(path, convert(string));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
