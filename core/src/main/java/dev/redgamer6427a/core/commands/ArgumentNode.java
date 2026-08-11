package dev.redgamer6427a.core.commands;

import lombok.Getter;

@Getter
public abstract class ArgumentNode<T> extends CommandNode {

    /**
     * The argument's name. Do not edit this.
     */
    private final String name;

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

    // BUG FIX (framework): equals() was overridden by name but hashCode() was not,
    // violating the equals/hashCode contract. Harmless by luck while depth/executor
    // lived on the node (same instance always used, so identity hashCode still worked
    // for the ExecutionContext#values map), but a landmine for any future HashMap/HashSet
    // usage keyed by ArgumentNode. Fixed to match equals().
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
