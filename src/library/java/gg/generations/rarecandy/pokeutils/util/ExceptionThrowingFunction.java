package gg.generations.rarecandy.pokeutils.util;

public interface ExceptionThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}

