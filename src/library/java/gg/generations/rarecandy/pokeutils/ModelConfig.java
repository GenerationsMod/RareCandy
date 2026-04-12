package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collector;

public class ModelConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient()
            .registerTypeAdapter(VariantParent.class, new VariantParent.Serializer())
            .registerTypeAdapter(MaterialReference.class, new MaterialReference.Serializer())
            .registerTypeAdapter(Vector2f.class, (JsonDeserializer<Vector2f>) (json, typeOfT, context) -> {
                var vec = new Vector2f();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 2) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat());
                    }
                } else if(json.isJsonObject()) {
                    var obj = json.getAsJsonObject();

                    if(obj.has("x")) vec.x = obj.getAsJsonPrimitive("x").getAsFloat();
                    if(obj.has("y")) vec.y = obj.getAsJsonPrimitive("y").getAsFloat();
                }

                return vec;
            })
            .registerTypeAdapter(VariantDetails.class, new GenericJsonThing<VariantDetails>((variantDetails, ctx) -> {
                var obj = new JsonObject();
                if (variantDetails.material() != null) obj.addProperty("material", variantDetails.material());
                if (variantDetails.hide() != null) obj.addProperty("hide", variantDetails.hide());
                if (variantDetails.paradox() != null) obj.addProperty("paradox", variantDetails.paradox());
                if (variantDetails.offset() != null && variantDetails.offset() != AnimationController.NO_OFFSET)
                    obj.add("transform", ctx.serialize(variantDetails.offset()));

                return obj;
            }, (jsonElement, ctx) -> {
                var obj = jsonElement.getAsJsonObject();
                var material = obj.has("material") ? obj.getAsJsonPrimitive("material").getAsString() : null;
                var effect = obj.has("effect") ? obj.getAsJsonPrimitive("effect").getAsString() : null;
                var paradox = obj.has("paradox") ? obj.getAsJsonPrimitive("paradox").getAsBoolean() : null;
                var hide = obj.has("hide") ? obj.getAsJsonPrimitive("hide").getAsBoolean() : null;
                Transform offset = obj.has("offset") ? ctx.deserialize(obj.get("offset"), Transform.class) : null;
                if(offset == null && obj.has("transform")) offset = ctx.deserialize(obj.get("transform"), Transform.class);
                return new VariantDetails(material, effect, paradox, hide, offset);
            }))
            .registerTypeAdapter(Transform.class, new GenericJsonThing<>((transform, ctx) -> {
                if (transform.scale().x == 1 && transform.scale().y == 1) {
                    return ctx.serialize(transform.offset());
                } else {
                    var obj = new JsonObject();
                    obj.add("scale", ctx.serialize(transform.scale()));
                    obj.add("offset", ctx.serialize(transform.offset()));
                    return obj;
                }
            }, (jsonElement, ctx) -> {
                if (jsonElement.isJsonArray()) return new Transform(ctx.deserialize(jsonElement, Vector2f.class));
                else {
                    var obj = jsonElement.getAsJsonObject();
                    Vector2f scale = ctx.deserialize(obj.get("scale"), Vector2f.class);
                    Vector2f offset = ctx.deserialize(obj.get("offset"), Vector2f.class);
                    return new Transform(scale, offset);
                }
            }))
            .registerTypeAdapter(Vector3f.class, new GenericJsonThing<Vector3f>((json, ctx) -> {
                int r = Math.min(255, Math.max(0, (int)(json.x * 255)));
                int g = Math.min(255, Math.max(0, (int)(json.y * 255)));
                int b = Math.min(255, Math.max(0, (int)(json.z * 255)));
                var string = String.format("#%02X%02X%02X", r, g, b);

                return new JsonPrimitive(string);
            }, (json, ctx) -> {
                var vec = new Vector3f();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 3) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat());
                    }
                } else if(json.isJsonObject()) {
                    var obj = json.getAsJsonObject();

                    if(obj.has("x")) vec.x = obj.getAsJsonPrimitive("x").getAsFloat();
                    if(obj.has("y")) vec.y = obj.getAsJsonPrimitive("y").getAsFloat();
                    if(obj.has("z")) vec.y = obj.getAsJsonPrimitive("z").getAsFloat();
                }


                return vec;
            }))
            .registerTypeAdapter(Quaternionf.class, new GenericJsonThing<Quaternionf>((json, ctx) -> {
                var array =  new JsonArray();
                array.add(json.x);
                array.add(json.y);
                array.add(json.z);
                array.add(json.w);
                return array;
            }, (json, ctx) -> {
                var vec = new Quaternionf();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 3) {
                        vec.rotationXYZ(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat());
                    } else if (json.getAsJsonArray().size() == 4) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat(), json.getAsJsonArray().get(3).getAsFloat());
                    }
                }

                return vec;
            }))
            .registerTypeAdapter(MeshOptions.class, new GenericJsonThing<>((meshOptions, ctx) -> {
                var json = new JsonObject();
                json.addProperty("invert", meshOptions.invert());
                json.add("aliases", meshOptions.aliases().stream().collect(Collector.of(
                        JsonArray::new, JsonArray::add,
                        (jsonElements, jsonElements2) -> {
                            jsonElements.addAll(jsonElements2);
                            return jsonElements;
                        }
                )));
                return json;
            }, (json, ctx) -> {
                var invert = false;
                var aliases = Collections.<String>emptyList();

                if (json.isJsonPrimitive()) invert = json.getAsBoolean();
                else if (json.isJsonArray())
                    aliases = json.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonPrimitive).map(JsonPrimitive::getAsString).toList();
                else {
                    var obj = json.getAsJsonObject();

                    if (obj.has("invert")) invert = obj.getAsJsonPrimitive("invert").getAsBoolean();
                    if (obj.has("aliases")) aliases = obj.getAsJsonArray("aliases").asList().stream().map(JsonElement::getAsJsonPrimitive).map(JsonPrimitive::getAsString).toList();
                }

                return new MeshOptions(invert, aliases);
            }))
            .registerTypeAdapter(SkeletalTransform.class, new GenericJsonThing<>((skeletalTransform, jsonSerializationContext) -> {
                var obj = new JsonObject();
                var position = skeletalTransform.position();
                if (position.x != 0 || position.y != 0 || position.z != 0) {
                    obj.add("position", jsonSerializationContext.serialize(position));
                }
                var rotation = skeletalTransform.rotation();
                if (rotation.x != 0 || rotation.y != 0 || rotation.z != 0 || rotation.w != 0) {
                    obj.add("rotation", jsonSerializationContext.serialize(rotation));
                }
                return obj;
            }, (element, context) -> {
                var obj = element.getAsJsonObject();

                var position = new Vector3f();

                if (obj.has("position")) {
                    position = context.deserialize(obj.get("position"), Vector3f.class);
                }

                var rotation = new Quaternionf();

                if (obj.has("rotation")) {
                    rotation = context.deserialize(obj.get("rotation"), Quaternionf.class);
                }

                return new SkeletalTransform(position, rotation);
            }))
            .create();
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantDetails> defaultVariant;
    public Map<String, VariantParent> variants;
    public Map<String, HideDuringAnimation> hideDuringAnimation = Collections.emptyMap();

    public Map<String, Integer> animationFpsOverride;
    public Map<String, Boolean> animationLoopsOverride; //TODO: Collaspse into a animationOverride map when have time

    public Map<String, SkeletalTransform> offsets = new HashMap<>();

    public Map<String, List<String>> materialsWithSameMaterialAnimation;

    public List<String> ignoreScaleInAnimation;

    public Map<String, MeshOptions> modelOptions;

    public List<String> meshesToRenderFirst;

    public Map<String, List<String>> aliases;

    public boolean excludeMeshNamesFromSkeleton = false;

    public Integer resolution;

    public static ModelConfig read(ResourceReader reader) throws IOException {
        return GSON.fromJson(new InputStreamReader(reader.getInputStream("config.json")), ModelConfig.class);
    }

    public List<String> getMaterialsForAnimation(String trackName) {
        var list = new ArrayList<String>();
        list.add(trackName);

        if(materialsWithSameMaterialAnimation != null) {
            if(materialsWithSameMaterialAnimation.containsKey(trackName)) {
                list.addAll(materialsWithSameMaterialAnimation.get(trackName));
            }
        }

        return list;
    }

    public record HideDuringAnimation(boolean blackList, List<String> animations) {
        public static final HideDuringAnimation NONE = new HideDuringAnimation();

        public HideDuringAnimation() {
            this(false, null);
        }

//        public boolean check(Animation animation) {
//            return check(animation != null ? animation.name : null);
//        }

        public boolean check(String animation) {
            if (animations != null)
                return animations.contains(animation) == blackList;
            return false;
        }
    }
}
