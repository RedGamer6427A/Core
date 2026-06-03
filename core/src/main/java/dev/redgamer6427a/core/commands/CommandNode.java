package dev.redgamer6427a.core.commands;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * The abstract root of all command tree parts.
 */
@Getter
public abstract class CommandNode {
    protected @Nullable CommandExecutor executor;
    @Setter
    private LiteralCommandNode parent;

    protected CommandNode(LiteralCommandNode parent, @Nullable CommandExecutor executor) {
        this.parent = parent;
        this.executor = executor;
    }

    /**
     *
     * @return this node's root node.
     */
    public ConsoleCommand getRootNode() {

        if (this instanceof ConsoleCommand) {
            return (ConsoleCommand) this;
        }

        if (parent == null) {
            throw new IllegalStateException("Parent is null!");
        }

        if (parent instanceof ConsoleCommand) {
            return (ConsoleCommand) parent;
        }

        return parent.getRootNode();


    }
}
