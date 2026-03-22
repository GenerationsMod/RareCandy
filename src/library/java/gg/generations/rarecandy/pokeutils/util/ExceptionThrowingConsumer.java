package gg.generations.rarecandy.pokeutils.util;

import java.util.function.Consumer;

public interface ExceptionThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
