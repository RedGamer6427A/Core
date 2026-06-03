package dev.redgamer6427a.core.utils;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E, InterruptedException;
}
