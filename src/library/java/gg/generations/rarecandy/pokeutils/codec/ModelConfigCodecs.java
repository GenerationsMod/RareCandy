package gg.generations.rarecandy.pokeutils.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.pokeutils.SkeletalTransform;
import gg.generations.rarecandy.pokeutils.material.MeshOptions;
import gg.generations.rarecandy.pokeutils.material.VariantDetails;
import gg.generations.rarecandy.pokeutils.material.VariantParent;
import gg.generations.rarecandy.pokeutils.util.Codecs;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static gg.generations.rarecandy.pokeutils.ModelConfig.HideDuringAnimation;

public class ModelConfigCodecs {
    public static final Codec<VariantDetails> VARIANT_DETAILS = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.nullable(Codec.STRING, "material", VariantDetails::material),
            Codecs.nullable(Codec.BOOL, "hide", VariantDetails::hide),
            Codecs.nullable(JomlCodecs.TRANSFORM, "offset", v -> {
                if (v.offset() == null || v.offset().equals(AnimationController.NO_OFFSET)) return null;
                return v.offset();
            })
    ).apply(instance, (Optional<String> material, Optional<Boolean> hide, Optional<Transform> offset) -> new VariantDetails(material.orElse(null), hide.orElse(null), offset.orElse(null))));

    public static final Codec<Map<String, VariantDetails>> VARIANT_DETAILS_MAP = Codecs.map(Codec.STRING, VARIANT_DETAILS);

    public static final Codec<VariantParent> VARIANT_PARENT = Codec.PASSTHROUGH.flatXmap(
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
                                .flatMap(entry -> VARIANT_DETAILS.encodeStart(ops, entry.getValue()).result().stream().map(val -> Pair.of(ops.createString(entry.getKey()), val)))
                );

                return DataResult.success(new Dynamic<>(ops, ops.createMap(entries)));
            }

    );

    public static final Codec<HideDuringAnimation> HIDE_DURING_ANIMATION = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("blackList", false).forGetter(HideDuringAnimation::blackList),
            Codec.STRING.listOf().optionalFieldOf("animations", List.of()).forGetter(HideDuringAnimation::animations)
    ).apply(instance, HideDuringAnimation::new));

    public static final Codec<SkeletalTransform> SKELETAL_TRANSFORM = Codec.PASSTHROUGH.flatXmap(dynamic -> {
        var position = dynamic.get("position").flatMap(JomlCodecs.VECTOR3F::parse).result().orElseGet(Vector3f::new);
        var rotation = dynamic.get("rotation").flatMap(JomlCodecs.QUATERNIONF::parse).result().orElseGet(Quaternionf::new);

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

    private static final Codec<MeshOptions> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(MeshOptions::invert),
            Codec.STRING.listOf().optionalFieldOf("aliases", List.of()).forGetter(MeshOptions::aliases)
    ).apply(instance, MeshOptions::new));

    public static final Codec<MeshOptions> MESH_OPTIONS = Codec.either(
            Codec.BOOL,
            Codec.either(Codec.STRING.listOf(), FULL_CODEC)
    ).xmap(
            either -> either.map(
                    bool -> {
                        return new MeshOptions(bool, List.of());
                    },
                    nested -> {
                        return nested.map(
                                aliases -> new MeshOptions(false, aliases),
                                Function.identity()
                        );
                    }
            ),
            meshOptions -> {
                if (meshOptions.aliases().isEmpty()) {
                    return Either.left(meshOptions.invert());
                } else if (!meshOptions.invert()) {
                    return Either.right(Either.left(meshOptions.aliases()));
                } else {
                    return Either.right(Either.right(meshOptions));
                }
            }
    );
}
