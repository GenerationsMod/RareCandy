package gg.generations.rarecandy.pokeutils.util;

@FunctionalInterface
public interface ExceptionThrowingTriFunction<T, U, V, R> {
    R apply(T t, U u, V v) throws Exception;
}
