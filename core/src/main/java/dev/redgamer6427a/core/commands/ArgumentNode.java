package dev.redgamer6427a.core.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class ArgumentNode<T> extends CommandNode {

    /**
     * The argument's name. Do not edit this.
     */
    private final String name;

    /**
     * The argument's parent's depth. Do not edit this.
     */
    @Setter
    private int depth = -1;

    /**
     * Constructor
     * @param parent the parent. Should be null.
     * @param name the argument's name.
     */
    protected ArgumentNode(LiteralCommandNode parent, String name) {
        super(parent, null);
        this.name = name;
    }

    /**
     * Set the argument's executor. Executed if this is the last provided argument.
     * @param executor the new executor
     */
    void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    /**
     * Parse the argument.
     * @param reader A reader for facilitated parsing.
     * @return A descriptive ParseResult instance.
     * @throws CommandSyntaxException if anything syntax-related went wrong.
     */
    protected abstract ParseResult<T> parse(ArgumentReader reader) throws CommandSyntaxException;

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof ArgumentNode)) {
            return false;
        }

        return name.equals(((ArgumentNode<?>) obj).name);
    }
}
