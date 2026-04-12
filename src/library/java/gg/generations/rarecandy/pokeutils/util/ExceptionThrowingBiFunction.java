package gg.generations.rarecandy.pokeutils.util;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ExceptionThrowingBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;
}

