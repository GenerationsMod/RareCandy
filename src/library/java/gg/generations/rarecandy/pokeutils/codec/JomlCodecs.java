package gg.generations.rarecandy.pokeutils.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.generations.rarecandy.renderer.animation.Transform;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.Function;

public class JomlCodecs {

    public static final Codec<Vector2f> VECTOR2F = Codec.FLOAT.listOf().flatXmap(
                    list -> list.size() >= 2 ? DataResult.success(new Vector2f(list.get(0), list.get(1))) : DataResult.error(() -> "Expected list of 2+ floats for Vector2f"),
                    v -> DataResult.success(List.of(v.x, v.y))
            );

    public static Codec<Vector3f> VECTOR3F = Codec.FLOAT.listOf().flatXmap(
            list -> list.size() >= 3 ? DataResult.success(new Vector3f(list.get(0), list.get(1), list.get(2))) : DataResult.error(() -> "Expected list of 3+ floats for Vector3f"),
            v -> DataResult.success(List.of(v.x, v.y, v.z))
    );

    public static final Codec<Vector4f> VECTOR4F = Codec.either(
            Codec.FLOAT.listOf().flatXmap(
                    list -> list.size() >= 4 ? DataResult.success(new Vector4f(list.get(0), list.get(1), list.get(2), list.get(3))) : DataResult.error(() -> "Expected list of 4+ floats for Vector4f"),
                    v -> DataResult.success(List.of(v.x, v.y, v.z, v.w))
            ),
            RecordCodecBuilder.<Vector4f>create(builder -> builder.group(
                    Codec.FLOAT.fieldOf("x").forGetter(v -> v.x),
                    Codec.FLOAT.fieldOf("y").forGetter(v -> v.y),
                    Codec.FLOAT.fieldOf("z").forGetter(v -> v.z),
                    Codec.FLOAT.fieldOf("w").forGetter(v -> v.w)
            ).apply(builder, Vector4f::new))
    ).xmap(
            either -> either.map(Function.identity(), Function.identity()),
            Either::left
    );

    public static final Codec<Quaternionf> QUATERNIONF = VECTOR3F.xmap(new Function<Vector3f, Quaternionf>() {
        @Override
        public Quaternionf apply(Vector3f vector3f) {
            return new Quaternionf().rotateXYZ(vector3f.x, vector3f.y, vector3f.z);
        }
    }, quaternionf -> quaternionf.getEulerAnglesXYZ(new Vector3f()));

    public static final Codec<Quaternionf> QUATERNIONF_OLD = Codec.either(
            Codec.FLOAT.listOf().flatXmap(
                    list -> {
                        if (list.size() == 3) {
                            Quaternionf q = new Quaternionf();
                            q.rotationXYZ(list.get(0), list.get(1), list.get(2));
                            return DataResult.success(q);
                        } else if (list.size() == 4) {
                            return DataResult.success(new Quaternionf(list.get(0), list.get(1), list.get(2), list.get(3)));
                        } else {
                            return DataResult.error(() -> "Expected list of 3 or 4 floats for Quaternionf");
                        }
                    },
                    q -> DataResult.success(List.of(q.x, q.y, q.z, q.w))
            ),
            RecordCodecBuilder.<Quaternionf>create(builder -> builder.group(
                    Codec.FLOAT.fieldOf("x").forGetter(q -> q.x),
                    Codec.FLOAT.fieldOf("y").forGetter(q -> q.y),
                    Codec.FLOAT.fieldOf("z").forGetter(q -> q.z),
                    Codec.FLOAT.fieldOf("w").forGetter(q -> q.w)
            ).apply(builder, Quaternionf::new))
    ).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    public static final Codec<Transform> TRANSFORM = RecordCodecBuilder.create(instance -> instance.group(
            VECTOR2F.optionalFieldOf("scale", Transform.DEFAULT_SCALE).forGetter(Transform::scale),
            VECTOR2F.optionalFieldOf("transform", Transform.DEFAULT_OFFSET).forGetter(Transform::offset)
    ).apply(instance, Transform::new));

}
