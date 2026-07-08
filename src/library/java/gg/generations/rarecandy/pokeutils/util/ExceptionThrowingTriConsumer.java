package gg.generations.rarecandy.pokeutils.util;

public interface ExceptionThrowingTriConsumer<T, R, U> {
    void accept(T t, R r, U u) throws Exception;
}
