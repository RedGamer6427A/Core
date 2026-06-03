package dev.redgamer6427a.core.utils;

import java.io.IOException;

/**
 * A BiFunction that throws IO Exceptions
 */
@FunctionalInterface
public interface IOThrowingBiFunction<T, U, R> {
    R apply(T t, U u) throws IOException;
}
