package dev.redgamer6427a.core.console.input;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An input hook. Calls an executor with raw key presses
 */
public class InputHook {

    /**
     * List of executors to be executed.
     */
    public List<Consumer<Character>> executors = new ArrayList<>();

    /**
     * Executed on closure of the hook.
     */
    private final Runnable onClose;

    /**
     * Whether this hook has been closed.
     */
    @Getter
    private boolean closed;

    /**
     * Add an executor that receives key presses.
     * @param consumer the executor
     */
    public void executor(Consumer<Character> consumer) {
        executors.add(consumer);

    }

    /**
     * Clears the executor list.
     */
    public void clearExecutors() {
        executors.clear();
    }

    /**
     * Call an executor.
     * @param character the detected character
     */
    @ApiStatus.Internal
    public void call(Character character) {
        if (closed) return;
        new ArrayList<>(executors).forEach(consumer -> consumer.accept(character));
    }

    /**
     * @param onClose run when the hook is closed via close()
     */
    public InputHook(Runnable onClose) {
        closed = false;
        this.onClose = onClose;
    }

    public void close() {
        closed = true;
        onClose.run();
        executors.clear();
    }

}
