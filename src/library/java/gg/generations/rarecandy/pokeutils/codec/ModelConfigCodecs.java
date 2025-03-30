package gg.generations.rarecandy.pokeutils.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.pokeutils.SkeletalTransform;
import gg.generations.rarecandy.pokeutils.material.*;
import gg.generations.rarecandy.pokeutils.util.Codecs;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static gg.generations.rarecandy.pokeutils.ModelConfig.HideDuringAnimation;

public class ModelConfigCodecs {
    public static final Codec<Vector2f> VECTOR2F_OLD = Codec.either(
            JomlCodecs.VECTOR2F,
            RecordCodecBuilder.<Vector2f>create(builder -> builder.group(
                    Codec.FLOAT.fieldOf("x").forGetter(v -> v.x),
                    Codec.FLOAT.fieldOf("y").forGetter(v -> v.y)
            ).apply(builder, Vector2f::new))
    ).xmap(
            either -> either.map(Function.identity(), Function.identity()),
            Either::left
    );
    public static final Codec<Vector3f> VECTOR3F_OLD = Codec.either(
            JomlCodecs.VECTOR3F,
            RecordCodecBuilder.<Vector3f>create(builder -> builder.group(
                    Codec.FLOAT.fieldOf("x").forGetter(v -> v.x),
                    Codec.FLOAT.fieldOf("y").forGetter(v -> v.y),
                    Codec.FLOAT.fieldOf("z").forGetter(v -> v.z)
            ).apply(builder, Vector3f::new))
    ).xmap(
            either -> either.map(Function.identity(), Function.identity()),
            Either::left
    );
    public static final Codec<SkeletalTransform> SKELETAL_TRANSFORM_OLD = Codec.PASSTHROUGH.flatXmap(dynamic -> {
        var position = dynamic.get("position").flatMap(VECTOR3F_OLD::parse).result().orElseGet(Vector3f::new);
        var rotation = dynamic.get("rotation").flatMap(JomlCodecs.QUATERNIONF_OLD::parse).result().orElseGet(Quaternionf::new);

        return DataResult.success(new SkeletalTransform(position, rotation));
    }, skeletalTransform -> {
        var ops = JsonOps.INSTANCE;
        var obj = new LinkedHashMap<JsonElement, JsonElement>();

        if (!skeletalTransform.position().equals(new Vector3f()))
            JomlCodecs.VECTOR3F.encodeStart(ops, skeletalTransform.position()).result()
                    .ifPresent(p -> obj.put(ops.createString("position"), p));

        if (!skeletalTransform.rotation().equals(new Quaternionf()))
            JomlCodecs.QUATERNIONF.encodeStart(ops, skeletalTransform.rotation()).result()
                    .ifPresent(r -> obj.put(ops.createString("rotation"), r));

        return DataResult.success(new Dynamic<>(ops, ops.createMap(obj)));
    });

    public static final Codec<Transform> TRANSFORM_BASE = RecordCodecBuilder.create(instance -> instance.group(
            VECTOR2F_OLD.optionalFieldOf("scale", Transform.DEFAULT_SCALE).forGetter(Transform::scale),
            VECTOR2F_OLD.optionalFieldOf("transform", Transform.DEFAULT_OFFSET).forGetter(Transform::offset)
    ).apply(instance, Transform::new));

    public static final Codec<Transform> TRANSFORM_OLD = Codec.either(
            VECTOR2F_OLD,
            TRANSFORM_BASE
    ).xmap(
            either -> either.map(Transform::new, Function.identity()),
            Either::right
    );
    public static Codec<Vector3f> OLD_COLOR_CODEC = Codec.of(new Encoder<>() {
        @Override
        public <T> DataResult<T> encode(Vector3f vector3f, DynamicOps<T> dynamicOps, T t) {
            int r = Math.round(clamp(vector3f.x()) * 255);
            int g = Math.round(clamp(vector3f.y()) * 255);
            int b = Math.round(clamp(vector3f.z()) * 255);
            String hex = String.format("#%02X%02X%02X", r, g, b);
            return DataResult.success(dynamicOps.createString(hex));
        }

        private float clamp(float v) {
            return Math.max(0f, Math.min(1f, v));
        }
    }, new Decoder<>() {
        @Override
        public <T> DataResult<Pair<Vector3f, T>> decode(DynamicOps<T> dynamicOps, T t) {
            var dynamic = new Dynamic<T>(dynamicOps, t);

            var list = dynamic.readList(Codec.FLOAT).result();

            if (list.isPresent()) {
                var values = list.get();

                float x = 1.0f, y = 1.0f, z = 1.0f;

                if (values.size() >= 3) {
                    x = values.get(0);
                    y = values.get(1);
                    z = values.get(2);
                }

                return DataResult.success(Pair.of(new Vector3f(x, y, z), t));
            }

            var string = dynamic.asString().result();

            if (string.isPresent()) {
                var value = string.get().replace("#", "");

                try {
                    int colorValue = Integer.parseInt(value, 16);
                    int r = (colorValue >> 16) & 0xFF;
                    int g = (colorValue >> 8) & 0xFF;
                    int b = colorValue & 0xFF;
                    return DataResult.success(Pair.of(new Vector3f(r / 255f, g / 255f, b / 255f), t));
                } catch (NumberFormatException ignored) {
                    return DataResult.error(() -> "Couldn't parse string.");
                }
            }

            var optionalX = dynamic.get("x").map(a -> a.asFloat(1.0f)).result();
            if (optionalX.isEmpty()) return DataResult.error(() -> "optionalX was empty.");
            var optionalY = dynamic.get("y").map(a -> a.asFloat(1.0f)).result();
            if (optionalY.isEmpty()) return DataResult.error(() -> "optionalY was empty.");
            var optionalZ = dynamic.get("z").map(a -> a.asFloat(1.0f)).result();
            if (optionalZ.isEmpty()) return DataResult.error(() -> "optionalZ was empty.");
            return DataResult.success(Pair.of(new Vector3f(optionalX.get(), optionalY.get(), optionalZ.get()), t));
        }
    });

    public static DataResult<Dynamic<?>> encodeValue(Object o) {
        if (o instanceof Boolean value) return DataResult.success(JsonOps.INSTANCE.createBoolean(value)).map(json -> new Dynamic<>(JsonOps.INSTANCE));
        else if (o instanceof Float value) return DataResult.success(JsonOps.INSTANCE.createFloat(value)).map(json -> new Dynamic<>(JsonOps.INSTANCE));
        else if (o instanceof Vector3f vec) return OLD_COLOR_CODEC.encodeStart(JsonOps.INSTANCE, vec).map(json -> new Dynamic<>(JsonOps.INSTANCE));
        else return DataResult.error(() -> "Unsupported value type for encoding: " + o.getClass().getName());
    }

    public static <T> DataResult<Object> decodeValue(Dynamic<T> dynamic) {
        DataResult<Object> boolResult = Codec.BOOL.parse(dynamic).map(Object.class::cast);
        if (boolResult.result().isPresent()) return boolResult;


        DataResult<Object> floatResult = Codec.FLOAT.parse(dynamic).map(Object.class::cast);
        if (floatResult.result().isPresent()) return floatResult;

        DataResult<Object> colorResult = OLD_COLOR_CODEC.parse(dynamic).map(Object.class::cast);
        if (colorResult.result().isPresent()) return colorResult;

        return DataResult.error(() -> "Unknown raw value type: " + dynamic);
    }

    public static Codec<Object> VALUE_CODEC_OLD = Codec.PASSTHROUGH.flatXmap(dynamic -> {
        return dynamic.get("type").asString().flatMap(type -> dynamic.get("value").flatMap(tDynamic -> switch (type) {
                    case "boolean" -> Codec.BOOL.parse(tDynamic).map(Object.class::cast);
                    case "float" -> Codec.FLOAT.parse(tDynamic).map(Object.class::cast);
                    case "color" -> OLD_COLOR_CODEC.parse(tDynamic).map(Object.class::cast);
                    default -> DataResult.error(() -> "Unknown type: " + type);
                })).result().map(DataResult::success)
                .orElseGet(() -> {
                    return ModelConfigCodecs.decodeValue(dynamic);
                }).map(v -> v);
    }, ModelConfigCodecs::encodeValue);

    public static final Codec<VariantDetails> VARIANT_DETAILS_OLD = Codecs.processing(RecordCodecBuilder.create(instance -> instance.group(
            Codecs.nullable(Codec.STRING, "material", VariantDetails::material),
            Codecs.nullable(Codec.BOOL, "hide", VariantDetails::hide),
            Codecs.optionalFieldOf(TRANSFORM_OLD, AnimationController.NO_OFFSET, "offset", "transform").forGetter(v -> {
                if (v.transform() == null || v.transform().equals(AnimationController.NO_OFFSET)) return null;
                return v.transform();
            })
    ).apply(instance, (Optional<String> material, Optional<Boolean> hide, Transform offset) -> new VariantDetails(material.orElse(null), hide.orElse(null), offset))), Function.identity(), Function.identity(), new Function<Dynamic<?>, Dynamic<?>>() {
        @Override
        public Dynamic<?> apply(Dynamic<?> dynamic) {
            return dynamic;
        }
    });

    public static final Codec<Map<String, VariantDetails>> VARIANT_DETAILS_MAP = Codecs.map(Codec.STRING, VARIANT_DETAILS_OLD);



    public static final Codec<VariantParent> VARIANT_PARENT_OLD = Codec.PASSTHROUGH.flatXmap(
            dynamic -> {
                var parent = dynamic.remove("inherits").asString().result().orElse(null);

                if(parent == null) parent = dynamic.remove("parent").asString().result().orElse(null);

                var details = VARIANT_DETAILS_MAP.parse(dynamic).result().orElseGet(HashMap::new);

                return DataResult.success(new VariantParent(parent, details));
            },
            variantParent -> {
                var ops = JsonOps.INSTANCE;

                var entries = Stream.concat(
                        variantParent.inherits() != null
                                ? Stream.of(Pair.of(ops.createString("parent"), ops.createString(variantParent.inherits())))
                                : Stream.empty(),
                        variantParent.details().entrySet().stream()
                                .flatMap(entry -> VariantDetails.CODEC.encodeStart(ops, entry.getValue()).result().stream().map(val -> {
                                    return Pair.of(ops.createString(entry.getKey()), val);
                                }))
                );

                return DataResult.success(new Dynamic<>(ops, ops.createMap(entries)));
            }

    );

    public static Codec<MaterialReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.nullable(Codec.STRING, "parent", a -> a.parent),
            Codecs.nullable(Codec.STRING, "shader", a -> a.shader),
            Codecs.nullable(Codec.STRING, "effect", a -> a.effect),
            Codecs.nullable(Codec.STRING.xmap(Function.identity(), String::toLowerCase).xmap(CullType::from, Enum::name), "cull", a -> a.cull),
            Codecs.nullable(Codec.STRING.xmap(Function.identity(), String::toLowerCase).xmap(BlendType::from, Enum::name), "blend", a -> a.blend),
            Codecs.nullable(Codecs.map(Codec.STRING, Codec.STRING), "images", a -> a.images == null || a.images.isEmpty() ? null : a.images),
            Codecs.nullable(Codecs.map(Codec.STRING, VALUE_CODEC_OLD), "values", a -> a.values == null || a.values.isEmpty() ? null : a.values)
    ).apply(instance, (Optional<String> parent, Optional<String> shader, Optional<String> effect, Optional<CullType> cull, Optional<BlendType> blend, Optional<Map<String, String>> images, Optional<Map<String, Object>> values) -> {
        return new MaterialReference(parent.orElse(null), shader.orElse(null), effect.orElse(null), cull.orElse(null), blend.orElse(null), images.orElse(null), values.orElse(null));
    }));


    public static final Codec<MaterialReference> OLD_MATERIAL_REFERNCE = Codecs.processing(CODEC, dynamic -> {
        var modifer = applyTypeModifier(dynamic);
        if (modifer.isPresent()) return modifer.get();

        var inherits = dynamic.remove("inherits");

        if (inherits.asString().result().isPresent()) {
            dynamic.set("parent", inherits);
        }

        return dynamic;
    }, Function.identity());

    public static final Codec<ModelConfig> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(c -> c.scale),
            Codecs.processing(Codecs.map(Codec.STRING, OLD_MATERIAL_REFERNCE), a -> a, ModelConfigCodecs::editEffects).fieldOf("materials").forGetter(c -> c.materials),
            Codecs.map(Codec.STRING, VARIANT_DETAILS_OLD).optionalFieldOf("defaultVariant",Map.of()).forGetter(c -> c.defaultVariant),
            Codecs.map(Codec.STRING, VARIANT_PARENT_OLD).optionalFieldOf("variants", Map.of()).forGetter(c -> c.variants),
            Codecs.map(Codec.STRING, HideDuringAnimation.CODEC).optionalFieldOf("hideDuringAnimation", Map.of()).forGetter(c -> c.hideDuringAnimation),
            Codecs.map(Codec.STRING, Codec.INT).optionalFieldOf("animationFpsOverride", Map.of()).forGetter(c -> c.animationFpsOverride),
            Codecs.map(Codec.STRING, SKELETAL_TRANSFORM_OLD).optionalFieldOf("offsets", Map.of()).forGetter(c -> c.offsets),
            Codecs.map(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("materialsWithSameMaterialAnimation", Map.of()).forGetter(c -> c.materialsWithSameMaterialAnimation),
            Codec.STRING.listOf().optionalFieldOf("ignoreScaleInAnimation", List.of()).forGetter(c -> c.ignoreScaleInAnimation),
            Codecs.map(Codec.STRING, MeshOptions.CODEC).optionalFieldOf("modelOptions", Map.of()).forGetter(c -> c.modelOptions),
            Codec.STRING.listOf().optionalFieldOf("meshesToRenderFirst", List.of()).forGetter(c -> c.meshesToRenderFirst),
            Codecs.map(Codec.STRING, Codec.STRING.listOf()).optionalFieldOf("aliases", Map.of()).forGetter(c -> c.aliases),
            Codec.BOOL.optionalFieldOf("excludeMeshNamesFromSkeleton", false).forGetter(c -> c.excludeMeshNamesFromSkeleton)
    ).apply(instance, (scale, materials, defaultVariant, variants, hideDuringAnimation, animationFpsOverride, offsets, materialsWithSameMaterialAnimation, ignoreScaleInAnimation, modelOptions, meshesToRenderFirst, aliases, excludeMeshNamesFromSkeleton) -> {
        var config = new ModelConfig();
        config.scale = scale;
        config.materials = materials;
        config.defaultVariant = defaultVariant;
        config.variants = variants;
        config.hideDuringAnimation = hideDuringAnimation;
        config.animationFpsOverride = animationFpsOverride;
        config.offsets = offsets;
        config.materialsWithSameMaterialAnimation = materialsWithSameMaterialAnimation;
        config.ignoreScaleInAnimation = ignoreScaleInAnimation;
        config.modelOptions = modelOptions;
        config.meshesToRenderFirst = meshesToRenderFirst;
        config.aliases = aliases;
        config.excludeMeshNamesFromSkeleton = excludeMeshNamesFromSkeleton;
        return config;
    }));

    public static final Codec<ModelConfig> OLD_CODEC = Codecs.processing(BASE_CODEC, new Function<Dynamic<?>, Dynamic<?>>() {
        @Override
        public Dynamic<?> apply(Dynamic<?> dynamic) {
            var variants = dynamic.get("variants");

            if(variants.result().isEmpty()) {
                dynamic = dynamic.set("variants", dynamic.emptyMap().set("regular", dynamic.emptyMap()));
            }

            return dynamic;
        }
    }, Function.identity());

    private static final Codec<MeshOptions> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(MeshOptions::invert),
            Codec.STRING.listOf().optionalFieldOf("aliases", List.of()).forGetter(a -> List.of())
    ).apply(instance, (invert, aliases) -> new MeshOptions(invert)));

    public static final Codec<MeshOptions> MESH_OPTIONS_OLD = Codec.either(
            Codec.BOOL,
            Codec.either(Codec.STRING.listOf(), FULL_CODEC)
    ).xmap(
            either -> either.map(
                    bool -> {
                        return new MeshOptions(bool);
                    },
                    nested -> {
                        return nested.map(
                                aliases -> new MeshOptions(false),
                                Function.identity()
                        );
                    }
            ),
            meshOptions -> {
                /*if (meshOptions.aliases().isEmpty()) {
                    return Either.left(meshOptions.invert());
                } else*/ if (!meshOptions.invert()) {
                    return Either.right(Either.left(List.of()));
                } else {
                    return Either.right(Either.right(meshOptions));
                }
            }
    );
    private static final List<String> EFFECTS = List.of("shadow", "galaxy", "sketch", "vintage", "pastel");


    public static Map<String, MaterialReference> editEffects(Map<String, MaterialReference> map) {
        var newMap = new HashMap<String, MaterialReference>();

        map.forEach((key, materialReference) -> {
            var effect = EFFECTS.stream().filter(key::contains).findFirst();
            if (effect.isEmpty() || !effect.get().equals(materialReference.effect)) {
                newMap.put(key, materialReference);
            } else {
                var parent = key.replace(effect.get() + "_", "");
                var updatedReference = new MaterialReference(parent, null, effect.get(), null, null, null, null);
                newMap.put(key, updatedReference);
            }

        });
        return newMap;
    }

    public static <T> Optional<Dynamic<T>> applyTypeModifier(Dynamic<T> dynamic) {
        var type = dynamic.get("type").asString().result();

        if (type.isPresent()) {
            var material = new Dynamic<>(dynamic.getOps());

            var texturesToAdd = new HashMap<Dynamic<?>, Dynamic<?>>();
            var valuesToAdd = new HashMap<Dynamic<?>, Dynamic<?>>();

            dynamic.get("texture").asString().result().ifPresent(a -> texturesToAdd.put(dynamic.createString("diffuse"), dynamic.createString(a)));

            switch (type.get()) {
                case "masked" -> {
                    var color = dynamic.get("color").decode(MaterialReference.COLOR_CODEC).result().map(Pair::getFirst).orElse(new Vector3f(1, 1, 1));

                    MaterialReference.COLOR_CODEC.encodeStart(dynamic.getOps(), color).result().map(a -> new Dynamic<T>(dynamic.getOps(), a)).ifPresent(colorDynamic -> {
                        valuesToAdd.put(dynamic.createString("color"), colorDynamic);
                    });

                    dynamic.get("mask").asString().result().ifPresent(texture -> {
                        texturesToAdd.put(dynamic.createString("mask"), dynamic.createString(texture));
                    });
                    material = material.set("shader", dynamic.createString("masked"));
                }
                case "transparent" -> material = material.set("blend", dynamic.createString("regular"));
                case "cull" -> material = material.set("cull", dynamic.createString("forward"));
                case "unlit_cull" -> {
                    material = material.set("cull", dynamic.createString("forward"));
                    valuesToAdd.put(dynamic.createString("useLight"), dynamic.createBoolean(false));
                }
                case "unlit" -> valuesToAdd.put(dynamic.createString("useLight"), dynamic.createBoolean(false));
            }

            if (!valuesToAdd.isEmpty()) material = material.set("images", dynamic.createMap(texturesToAdd));

            if (!valuesToAdd.isEmpty()) material = material.set("values", dynamic.createMap(valuesToAdd));

            return Optional.of(material);
        }

        return Optional.empty();
    }
}
