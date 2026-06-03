package dev.redgamer6427a.core.console.input;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KittyInputHook {

    private final List<Consumer<KittyTerminalInput.KeyEvent>> keyExecutors = new ArrayList<>();
    private final List<Consumer<KittyTerminalInput.MouseEvent>> mouseExecutors = new ArrayList<>();

    private final Runnable onClose;
    /**
     * -- GETTER --
     *  Check if this hook is closed.
     *
     */
    @Getter
    private boolean closed;

    /**
     * Register a keyboard event executor.
     *
     * @param consumer The consumer to handle KeyEvents
     */
    public void onKeyExecutor(Consumer<KittyTerminalInput.KeyEvent> consumer) {
        if (!closed) {
            keyExecutors.add(consumer);
        }
    }

    /**
     * Register a mouse event executor.
     *
     * @param consumer The consumer to handle MouseEvents
     */
    public void onMouseExecutor(Consumer<KittyTerminalInput.MouseEvent> consumer) {
        if (!closed) {
            mouseExecutors.add(consumer);
        }
    }

    /**
     * Generic executor registration (for compatibility with InputHook interface).
     * Note: This is not recommended; use onKeyExecutor or onMouseExecutor instead.
     *
     * @param consumer The consumer (should handle Object events)
     */

    public void executor(Consumer<Object> consumer) {
        // Not implemented for generic case — use type-specific methods
    }

    /**
     * Clear all registered key executors.
     */
    public void clearKeyExecutors() {
        keyExecutors.clear();
    }

    /**
     * Clear all registered mouse executors.
     */
    public void clearMouseExecutors() {
        mouseExecutors.clear();
    }

    /**
     * Clear all executors (both key and mouse).
     */

    public void clearExecutors() {
        keyExecutors.clear();
        mouseExecutors.clear();
    }

    /**
     * Dispatch a keyboard event to all registered key executors.
     *
     * @param keyEvent The key event to dispatch
     */
    public void onKey(KittyTerminalInput.KeyEvent keyEvent) {
        if (closed) return;
        keyExecutors.forEach(consumer -> consumer.accept(keyEvent));
    }

    /**
     * Dispatch a mouse event to all registered mouse executors.
     *
     * @param mouseEvent The mouse event to dispatch
     */
    public void onMouse(KittyTerminalInput.MouseEvent mouseEvent) {
        if (closed) return;
        mouseExecutors.forEach(consumer -> consumer.accept(mouseEvent));
    }

    /**
     * Generic call method (for compatibility with InputHook interface).
     *
     * @param event The event (should be KeyEvent or MouseEvent)
     */

    public void call(Object event) {
        if (event instanceof KittyTerminalInput.KeyEvent keyEvent) {
            onKey(keyEvent);
        } else if (event instanceof KittyTerminalInput.MouseEvent mouseEvent) {
            onMouse(mouseEvent);
        }
    }

    /**
     * Construct a new KittyInputHook.
     *
     * @param onClose Callback to invoke when the hook is closed
     */
    KittyInputHook(Runnable onClose) {
        this.closed = false;
        this.onClose = onClose;
    }

    /**
     * Close the hook and clean up resources.
     */

    public void close() {
        closed = true;
        onClose.run();
        keyExecutors.clear();
        mouseExecutors.clear();
    }
}