package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.util.JomlConstants;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.model.material.IMaterialImages;
import gg.generations.rarecandy.renderer.model.material.IMaterialValues;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface IMaterialReference {
    IMaterialReference DEFAULT = new IMaterialReference() {
        @Override
        public String parent() {
            return null;
        }

        @Override
        public String shader() {
            return "";
        }

        @Override
        public CullType cull() {
            return null;
        }

        @Override
        public BlendType blend() {
            return null;
        }

        @Override
        public IMaterialImages images() {
            return null;
        }

        @Override
        public IMaterialValues values() {
            return null;
        }
    };

    String parent();
    String shader();
    CullType cull();
    BlendType blend();
    IMaterialImages images();
    IMaterialValues values();

    static IMaterialReference complete(String name, Map<String, IMaterialReference> materialReferenceMap) {
        IMaterialReference reference = materialReferenceMap.getOrDefault(name, IMaterialReference.DEFAULT);

        String parent = reference.parent();
        String shader = reference.shader();
        CullType cull = reference.cull();
        BlendType blend = reference.blend();
        var images = reference.images();
        String diffuse = images.diffuse();
        String layer = images.layer();
        String mask = images.mask();
        String emission = images.emission();
        var values = reference.values();
        Vector3f baseColor1 = values.baseColor1();
        Vector3f baseColor2 = values.baseColor2();
        Vector3f baseColor3 = values.baseColor3();
        Vector3f baseColor4 = values.baseColor4();
        Vector3f baseColor5 = values.baseColor5();
        Vector3f emiColor1 = values.emiColor1();
        Vector3f emiColor2 = values.emiColor2();
        Vector3f emiColor3 = values.emiColor3();
        Vector3f emiColor4 = values.emiColor4();
        Vector3f emiColor5 = values.emiColor5();
        float emiIntensity1 = values.emiIntensity1();
        float emiIntensity2 = values.emiIntensity2();
        float emiIntensity3 = values.emiIntensity3();
        float emiIntensity4 = values.emiIntensity4();
        float emiIntensity5 = values.emiIntensity5();
        boolean useLight = values.useLight();
        boolean disableDepth = values.disableDepth();

        while (parent != null) {
            reference = materialReferenceMap.get(parent);

            if (reference == null) parent = null;
            else {
                shader = replaceIfNullOrNotEquals(shader, reference.shader());
                cull = replaceIfNullOrNotEquals(cull, reference.cull());
                blend = replaceIfNullOrNotEquals(blend, reference.blend());
                images = reference.images();
                diffuse = replaceIfNullOrNotEquals(diffuse, images.diffuse());
                layer = replaceIfNullOrNotEquals(layer, images.layer());
                mask = replaceIfNullOrNotEquals(mask, images.mask());
                emission = replaceIfNullOrNotEquals(emission, images.emission());
                values = reference.values();
                baseColor1 = replaceIfNullOrNotEquals(baseColor1, values.baseColor1());
                baseColor2 = replaceIfNullOrNotEquals(baseColor2, values.baseColor2());
                baseColor3 = replaceIfNullOrNotEquals(baseColor3, values.baseColor3());
                baseColor4 = replaceIfNullOrNotEquals(baseColor4, values.baseColor4());
                baseColor5 = replaceIfNullOrNotEquals(baseColor5, values.baseColor5());
                emiColor1 = replaceIfNullOrNotEquals(emiColor1, values.emiColor1());
                emiColor2 = replaceIfNullOrNotEquals(emiColor2, values.emiColor2());
                emiColor3 = replaceIfNullOrNotEquals(emiColor3, values.emiColor3());
                emiColor4 = replaceIfNullOrNotEquals(emiColor4, values.emiColor4());
                emiColor5 = replaceIfNullOrNotEquals(emiColor5, values.emiColor5());
                emiIntensity1 = replaceIfNullOrNotEquals(emiIntensity1, values.emiIntensity1());
                emiIntensity2 = replaceIfNullOrNotEquals(emiIntensity2, values.emiIntensity2());
                emiIntensity3 = replaceIfNullOrNotEquals(emiIntensity3, values.emiIntensity3());
                emiIntensity4 = replaceIfNullOrNotEquals(emiIntensity4, values.emiIntensity4());
                emiIntensity5 = replaceIfNullOrNotEquals(emiIntensity5, values.emiIntensity5());
                useLight = replaceIfNullOrNotEquals(useLight, values.useLight());
                disableDepth = replaceIfNullOrNotEquals(disableDepth, values.disableDepth());

                parent = reference.parent();
            }
        }

        return IModelConfig.Factory.ACTIVE_FACTORY.createMaterialReference(
                null, shader, cull, blend,
                diffuse, layer, mask, emission,
                baseColor1, baseColor2, baseColor3, baseColor4, baseColor5,
                emiColor1, emiColor2, emiColor3, emiColor4, emiColor5,
                emiIntensity1, emiIntensity2, emiIntensity3, emiIntensity4, emiIntensity5,
                useLight, disableDepth
        );
    }

    private static <T> T replaceIfNullOrNotEquals(T prevValue, T newValue) {
        return newValue == null || Objects.equals(prevValue, newValue) ? prevValue : newValue;
    }

//    public static long getHandle(ITextureLoader loader, String name) {
//        ITexture tex = loader.getTexture(name);
//
//        return tex == null ? 0L : tex.getSamplerHandle(SamplerPresets.NEAREST_REPEAT);
//    }

    static Material process(IMaterialReference reference, List<String> imageNames) {
        var images = reference.images().toArray(imageNames);

        int method = 0;
        if (reference.shader() != null) {
            method = switch (reference.shader()) {
                case "layered" -> 1;
                case "masked" -> 2;
                default -> 0;
            };
        }

        return new Material(
                images,
                reference.values(),
                reference.cull(),
                reference.blend(),
                method
        );
    }


    static IMaterialReference deserialize(JsonElement json) {

        String shader = "solid";

        CullType cull = CullType.None;

        BlendType blend = BlendType.None;

        String diffuse = IMaterialImages.DEFAULT.diffuse();
        String layer = IMaterialImages.DEFAULT.layer();
        String mask = IMaterialImages.DEFAULT.mask();
        String emission = IMaterialImages.DEFAULT.emission();

        Vector3f baseColor1 = IMaterialValues.DEFAULT.baseColor1();
        Vector3f baseColor2 = IMaterialValues.DEFAULT.baseColor2();
        Vector3f baseColor3 = IMaterialValues.DEFAULT.baseColor3();
        Vector3f baseColor4 = IMaterialValues.DEFAULT.baseColor4();
        Vector3f baseColor5 = IMaterialValues.DEFAULT.baseColor5();
        Vector3f emiColor1 = IMaterialValues.DEFAULT.emiColor1();
        Vector3f emiColor2 = IMaterialValues.DEFAULT.emiColor2();
        Vector3f emiColor3 = IMaterialValues.DEFAULT.emiColor3();
        Vector3f emiColor4 = IMaterialValues.DEFAULT.emiColor4();
        Vector3f emiColor5 = IMaterialValues.DEFAULT.emiColor5();
        float emiIntensity1 = IMaterialValues.DEFAULT.emiIntensity1();
        float emiIntensity2 = IMaterialValues.DEFAULT.emiIntensity2();
        float emiIntensity3 = IMaterialValues.DEFAULT.emiIntensity3();
        float emiIntensity4 = IMaterialValues.DEFAULT.emiIntensity4();
        float emiIntensity5 = IMaterialValues.DEFAULT.emiIntensity5();
        boolean useLight = IMaterialValues.DEFAULT.useLight();
        boolean disableDepth = IMaterialValues.DEFAULT.disableDepth();

        var jsonObject = json.getAsJsonObject();

        String parent = jsonObject.has("inherits") ? jsonObject.get("inherits").getAsString() : jsonObject.has("parent") ? jsonObject.get("parent").getAsString() : null;

        if (jsonObject.has("type")) {

            var type = jsonObject.getAsJsonPrimitive("type").getAsString();

            if (jsonObject.has("texture")) {

                diffuse = jsonObject.getAsJsonPrimitive("texture").getAsString();

                if (type.contains("masked")) {
                    var color = jsonObject.has("color") ? color(jsonObject.get("color")) : new Vector3f(1.0f, 1.0f, 1.0f);
                    shader = "masked";
                    baseColor1 = color;
                    mask = jsonObject.getAsJsonPrimitive("mask").getAsString();
                } else {
                    switch (type) {
                        case "transparent" -> blend = BlendType.Regular;
                        case "cull" -> cull = CullType.Forward;
                        case "unlit_cull" -> {
                            cull = CullType.Forward;
                            useLight = false;
                        }
                        case "unlit" -> useLight = false;
                    }
                }
            }
        } else {
            if (jsonObject.has("shader")) {
                shader = jsonObject.getAsJsonPrimitive("shader").getAsString();

                switch (shader) {
                    case "masked_paradox" -> {
                        shader = "masked";
                    }
                    case "paradox", "solid_paradox" -> {
                        shader = "solid";
                    }
                }
            }

            cull = JsonUtils.extractIfPresent(jsonObject, "cull", cull, CullType::fromJson);
            blend = JsonUtils.extractIfPresent(jsonObject, "blend", blend, BlendType::fromJson);

            if (jsonObject.has("images")) {
                var images = jsonObject.getAsJsonObject("images");
                diffuse = JsonUtils.extractIfPresent(images, "diffuse", diffuse, JsonElement::getAsString);
                layer = JsonUtils.extractIfPresent(images, "layer", layer, JsonElement::getAsString);
                mask = JsonUtils.extractIfPresent(images, "mask", mask, JsonElement::getAsString);
                emission = JsonUtils.extractIfPresent(images, "emission", emission, JsonElement::getAsString);
            }

            if (jsonObject.has("values")) {
                var values = jsonObject.getAsJsonObject("values");
                baseColor1 = JsonUtils.extractIfPresent(values, "color", baseColor1, IMaterialReference::color);
                baseColor1 = JsonUtils.extractIfPresent(values, "baseColor1", baseColor1, IMaterialReference::color);
                baseColor2 = JsonUtils.extractIfPresent(values, "baseColor2", baseColor2, IMaterialReference::color);
                baseColor3 = JsonUtils.extractIfPresent(values, "baseColor3", baseColor3, IMaterialReference::color);
                baseColor4 = JsonUtils.extractIfPresent(values, "baseColor4", baseColor4, IMaterialReference::color);
                baseColor5 = JsonUtils.extractIfPresent(values, "baseColor5", baseColor5, IMaterialReference::color);
                emiColor1 = JsonUtils.extractIfPresent(values, "emiColor1", emiColor1, IMaterialReference::color);
                emiColor2 = JsonUtils.extractIfPresent(values, "emiColor2", emiColor2, IMaterialReference::color);
                emiColor3 = JsonUtils.extractIfPresent(values, "emiColor3", emiColor3, IMaterialReference::color);
                emiColor4 = JsonUtils.extractIfPresent(values, "emiColor4", emiColor4, IMaterialReference::color);
                emiColor5 = JsonUtils.extractIfPresent(values, "emiColor5", emiColor5, IMaterialReference::color);
                emiIntensity1 = JsonUtils.extractIfPresent(values, "emiIntensity1", emiIntensity1, JsonElement::getAsFloat);
                emiIntensity2 = JsonUtils.extractIfPresent(values, "emiIntensity2", emiIntensity2, JsonElement::getAsFloat);
                emiIntensity3 = JsonUtils.extractIfPresent(values, "emiIntensity3", emiIntensity3, JsonElement::getAsFloat);
                emiIntensity4 = JsonUtils.extractIfPresent(values, "emiIntensity4", emiIntensity4, JsonElement::getAsFloat);
                emiIntensity5 = JsonUtils.extractIfPresent(values, "emiIntensity5", emiIntensity5, JsonElement::getAsFloat);
                useLight = JsonUtils.extractIfPresent(values, "useLight", useLight, JsonElement::getAsBoolean);
                disableDepth = JsonUtils.extractIfPresent(values, "disableDepth", disableDepth, JsonElement::getAsBoolean);
            }
        }

        return IModelConfig.Factory.ACTIVE_FACTORY.createMaterialReference(parent, shader,
                cull, blend,
                diffuse, layer, mask, emission,
                baseColor1, baseColor2, baseColor3, baseColor4, baseColor5,
                emiColor1, emiColor2, emiColor3, emiColor4, emiColor5,
                emiIntensity1, emiIntensity2, emiIntensity3, emiIntensity4, emiIntensity5,
                useLight, disableDepth
        );
    }

    static JsonObject serialize(IMaterialReference materialReference) {
        var object = new JsonObject();
        JsonUtils.putIf(Objects::nonNull, object, "parent", materialReference.parent(), JsonPrimitive::new);
        JsonUtils.putIf(obj -> Objects.nonNull(obj) && (!obj.isBlank() || obj.equalsIgnoreCase("solid")), object, "shader", materialReference.shader(), JsonPrimitive::new);
        JsonUtils.putIf(cull -> Objects.nonNull(cull) && cull != CullType.None, object, "cull", materialReference.cull(), CullType::toJson);
        JsonUtils.putIf(blend -> Objects.nonNull(blend) && blend != BlendType.None, object, "blend", materialReference.blend(), BlendType::toJson);

        JsonUtils.putIf(IMaterialImages::isNotEmpty, object, "images", materialReference.images(), IMaterialImages::serialize);
        JsonUtils.putIf(IMaterialValues::isNotEmpty, object, "values", materialReference.values(), IMaterialValues::serialize);
        return object;
    }

    static Vector3f color(JsonElement element) {
        if(element.isJsonArray()) {
            var array = element.getAsJsonArray();
            return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        } else if(element.isJsonPrimitive()){
            int colorValue = Integer.parseInt(element.getAsString().replace("#", ""), 16);

            // Extract individual R, G, and B components
            int red = (colorValue >> 16) & 0xFF;
            int green = (colorValue >> 8) & 0xFF;
            int blue = colorValue & 0xFF;

            // Normalize the RGB components to a range of 0.0 to 1.0 for Vector3f
            float r = red / 255.0f;
            float g = green / 255.0f;
            float b = blue / 255.0f;

            // Create and return the Vector3f object
            return new Vector3f(r, g, b);
        } else {
            return JomlConstants.VECTOR3F_ONE;
        }
    }


    interface Factory {
        IMaterialImages createMaterialImages(String diffuse, String layer, String mask, String emission);

        IMaterialValues createMaterialValues(
                Vector3f baseColor1,
                Vector3f baseColor2,
                Vector3f baseColor3,
                Vector3f baseColor4,
                Vector3f baseColor5,
                Vector3f emiColor1,
                Vector3f emiColor2,
                Vector3f emiColor3,
                Vector3f emiColor4,
                Vector3f emiColor5,
                float emiIntensity1,
                float emiIntensity2,
                float emiIntensity3,
                float emiIntensity4,
                float emiIntensity5,
                boolean useLight,
                boolean disableDepth
        );

        default IMaterialReference createMaterialReference(
                String parent, String shader,
                CullType cull, BlendType blend,
                String diffuse, String layer, String mask, String emission,
                Vector3f baseColor1, Vector3f baseColor2, Vector3f baseColor3, Vector3f baseColor4, Vector3f baseColor5,
                Vector3f emiColor1, Vector3f emiColor2, Vector3f emiColor3, Vector3f emiColor4, Vector3f emiColor5,
                float emiIntensity1, float emiIntensity2, float emiIntensity3, float emiIntensity4, float emiIntensity5,
                boolean useLight, boolean disableDepth
        ) {
            return createMaterialReference(parent, shader, cull, blend,
                    createMaterialImages(diffuse, layer, mask, emission),
                    createMaterialValues(
                            baseColor1, baseColor2, baseColor3, baseColor4, baseColor5,
                            emiColor1, emiColor2, emiColor3, emiColor4, emiColor5,
                            emiIntensity1, emiIntensity2, emiIntensity3, emiIntensity4, emiIntensity5,
                            useLight, disableDepth
                    )
            );
        }

        IMaterialReference createMaterialReference(
                String parent, String shader,
                CullType cull, BlendType blend,
                IMaterialImages materialImages, IMaterialValues materialValues);
    }
}
