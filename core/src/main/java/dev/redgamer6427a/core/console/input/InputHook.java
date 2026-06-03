package dev.redgamer6427a.core.console.input;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InputHook {

    public List<Consumer<Character>> executors  = new ArrayList<>();

    private final Runnable onClose;
    @Getter
    private boolean closed;

    public void executor(Consumer<Character> consumer) {
        executors.add(consumer);

    }

    public void clearExecutors() {
        executors.clear();
    }

    public void call(Character character) {
        if (closed) return;
        new ArrayList<>(executors).forEach(consumer -> consumer.accept(character));
    }

    InputHook(Runnable onClose) {
        closed = false;
        this.onClose = onClose;
    }

    public void close() {
        closed = true;
        onClose.run();
        executors.clear();
    }

}
