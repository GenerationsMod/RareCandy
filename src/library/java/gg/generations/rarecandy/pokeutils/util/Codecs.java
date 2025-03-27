package gg.generations.rarecandy.pokeutils.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class Codecs {
    public static <A> MapCodec<A> optionalFieldOf(Codec<A> codec, A defaultValue, String... names) {
        return optionalField(codec, names).xmap((o) -> {
            return o.orElse(defaultValue);
        }, (a) -> {
            return Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a);
        });
    }

    static <F> MapCodec<Optional<F>> optionalField(Codec<F> elementCodec, String... names) {
        return new OptionalFieldCodec<F>(elementCodec, names);
    }

    public static <T> Codec<T> processing(Codec<T> codec, Function<Dynamic<?>, Dynamic<?>> pre, Function<T, T> post) {
        return Codec.PASSTHROUGH.xmap(pre, Function.identity()).flatXmap(codec::parse, t -> codec.encodeStart(JsonOps.INSTANCE, post.apply(t)).map(a -> new Dynamic<>(JsonOps.INSTANCE, a)));
    }

    public static class OptionalFieldCodec<A> extends MapCodec<Optional<A>> {
        private final String[] names;
        private final Codec<A> elementCodec;

        public OptionalFieldCodec(Codec<A> elementCodec, String... names) {
            this.names = names;
            this.elementCodec = elementCodec;
        }

        public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
            for (String name : names) {
                T value = input.get(name);

                if (value != null) {
                    DataResult<A> parsed = this.elementCodec.parse(ops, value);
                    return parsed.result().isPresent() ? parsed.map(Optional::of) : DataResult.success(Optional.empty());
                }
            }

            return DataResult.success(Optional.empty());
        }

        public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.isPresent() ? prefix.add(this.names[0], this.elementCodec.encodeStart(ops, input.get())) : prefix;
        }

        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(this.names).map(ops::createString);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                OptionalFieldCodec<?> that = (OptionalFieldCodec<?>) o;
                return Arrays.equals(this.names, that.names) && Objects.equals(this.elementCodec, that.elementCodec);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(Arrays.hashCode(this.names), this.elementCodec);
        }

        public String toString() {
            return "OptionalFieldCodec[" + Arrays.toString(this.names) + ": " + this.elementCodec + "]";
        }
    }

    public static <K, V> Codec<Map<K, V>> map(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new UnboundedMapCodec<>(keyCodec, elementCodec);
    }

    public static <A> Codec<A> nullable(Codec<A> codec) {
        return new NullableCodec<>(codec);
    }

    public static <A, O> RecordCodecBuilder<O, Optional<A>> nullable(Codec<A> codec, String field, Function<O, A> function) {
        return codec.optionalFieldOf(field).<O>forGetter(a -> function.andThen(Optional::ofNullable).apply(a));
    }

    public record UnboundedMapCodec<K, V>(Codec<K> keyCodec,
                                          Codec<V> elementCodec) implements BaseMapCodec<K, V>, Codec<Map<K, V>> {

        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap((map) -> {
                return this.decode(ops, map);
            }).map((r) -> {
                return Pair.of(r, input);
            });
        }

        public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
            return this.encode(input, ops, ops.mapBuilder()).build(prefix);
        }

        @Override
        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
            var read = new HashMap<K, V>();
            ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();
            DataResult<Unit> result = input.entries().reduce(DataResult.success(Unit.INSTANCE, Lifecycle.stable()), (r, pair) -> {
                DataResult<K> k = this.keyCodec().parse(ops, pair.getFirst());

                DataResult<V> v;

                v = this.elementCodec().parse(ops, pair.getSecond());

                DataResult<Pair<K, V>> entry = k.apply2stable(Pair::of, v);
                entry.error().ifPresent((e) -> {
                    failed.add(pair);
                });
                return r.apply2stable((u, p) -> {
                    read.put(p.getFirst(), p.getSecond());
                    return u;
                }, entry);
            }, (r1, r2) -> {
                return r1.apply2stable((u1, u2) -> {
                    return u1;
                }, r2);
            });
            T errors = ops.createMap(failed.build().stream());
            return result.map((unit) -> (Map<K, V>) read).setPartial(read).mapError((e) -> e + " missed input: " + errors);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                UnboundedMapCodec<?, ?> that = (UnboundedMapCodec<?, ?>) o;
                return Objects.equals(this.keyCodec, that.keyCodec) && Objects.equals(this.elementCodec, that.elementCodec);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.keyCodec, this.elementCodec});
        }

        public String toString() {
            return "UnboundedMapCodec[" + this.keyCodec + " -> " + this.elementCodec + "]";
        }
    }

    public record NullableCodec<U>(Codec<U> wrapped) implements Codec<U> {

        public <T> DataResult<T> encode(U input, DynamicOps<T> ops, T prefix) {
            return input == null ? DataResult.success(ops.empty()) : wrapped.encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<U, T>> decode(DynamicOps<T> ops, T input) {
            return input == ops.empty() ? DataResult.success(Pair.of(null, input)) : wrapped.decode(ops, input);
        }
    }
}
