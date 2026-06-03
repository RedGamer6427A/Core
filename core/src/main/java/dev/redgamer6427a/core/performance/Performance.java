package dev.redgamer6427a.core.performance;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class Performance {

    /**
     * Measure an action's completion time.
     * @param runnable the action to measure.
     * @return the completion time in milliseconds.
     */
    public static @Range(from=0, to=Long.MAX_VALUE) long measure(@NotNull Runnable runnable){
        long start = System.currentTimeMillis();

        runnable.run();

        long end = System.currentTimeMillis();
        return end - start;
    }
}
