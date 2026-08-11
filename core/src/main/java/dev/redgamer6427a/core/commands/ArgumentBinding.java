package dev.redgamer6427a.core.commands;

import lombok.Getter;
import lombok.Setter;

/**
 * Binds an {@link ArgumentNode} to one specific position within one specific
 * syntax registration (one {@code addSyntax(...)} call).
 * <p>
 * ArgumentNode instances are routinely reused across multiple {@code addSyntax(...)}
 * calls (e.g. the same "id" argument shared by "start" and "quickstart"). Depth and
 * executor are properties of a *registration*, not of the node itself — storing them
 * directly on the shared node meant the later registration silently overwrote the
 * earlier one's depth/executor, causing the wrong executor to fire and corrupting
 * index math for unrelated syntax paths. This wrapper fixes that by giving each
 * registration its own binding.
 *
 * @param <T> the type this argument parses to.
 */
@Getter
public final class ArgumentBinding<T> {

    private final ArgumentNode<T> node;
    private final int depth;
    @Setter
    private CommandExecutor executor;

    ArgumentBinding(ArgumentNode<T> node, int depth) {
        this.node = node;
        this.depth = depth;
    }
}
