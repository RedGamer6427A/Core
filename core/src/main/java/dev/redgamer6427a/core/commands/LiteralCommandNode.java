package dev.redgamer6427a.core.commands;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yet another abstract class.
 */
@Getter
public class LiteralCommandNode extends CommandNode {

    private final Map<String, LiteralCommandNode> children = new HashMap<>();
    // BUG FIX (framework): was List<ArgumentNode<?>>. Arguments are now stored wrapped
    // in ArgumentBinding so depth/executor are per-registration, not mutated on the
    // shared ArgumentNode instance. See ArgumentBinding.
    private final List<ArgumentBinding<?>> arguments = new ArrayList<>();
    private final String name;


    LiteralCommandNode(@Nullable LiteralCommandNode parent, @Nullable CommandExecutor executor, String name){
        super(parent, executor);
        this.name = name;
    }

    protected void addChild(LiteralCommandNode child){
        children.put(child.name, child);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof LiteralCommandNode literalNode)) return false;

        return name.equals(literalNode.name);
    }


    public String asString(String prefix, boolean isRoot) {
        StringBuilder builder = new StringBuilder();

        // Print current node
        if (isRoot) builder.append(name).append("\n");

        List<LiteralCommandNode> nodes = new ArrayList<>(children.values());
        for (int i = 0; i < nodes.size(); i++) {
            LiteralCommandNode child = nodes.get(i);
            boolean isLast = (i == nodes.size() - 1);

            builder.append(prefix)
                    .append(isLast ? "└─ " : "├─ ")
                    .append(child.getName());

            // Print arguments if any

            if (child.executor != null) {
                builder.append(" (exec)");
            }

            if (!child.getArguments().isEmpty()) {
                builder.append(" ");
                for (ArgumentBinding<?> binding : child.getArguments()) {
                    builder.append(binding.getNode().getClass().getCanonicalName());

                    if (binding.getExecutor() != null) {
                        builder.append(" (exec)");
                    }
                    builder.append(", ");
                }
                // BUG FIX: each entry appends ", " (2 chars), but only 1 was being trimmed,
                // leaving a dangling comma (e.g. "StringArgument, IntegerArgument,").
                builder.setLength(builder.length() - 2);
            }

            builder.append("\n");

            // Recurse into children with adjusted prefix
            String newPrefix = prefix + (isLast ? "   " : "│  ");
            builder.append(child.asString(newPrefix, false));
        }

        return builder.toString();
    }



}
