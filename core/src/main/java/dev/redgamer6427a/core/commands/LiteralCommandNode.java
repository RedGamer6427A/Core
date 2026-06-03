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
    private final List<ArgumentNode<?>> arguments = new ArrayList<>();
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
                for (ArgumentNode<?> arg : child.getArguments()) {
                    builder.append(arg.getClass().getCanonicalName());

                    if (arg.getExecutor() != null) {
                        builder.append(" (exec)");
                    }
                    builder.append(", ");
                }
                builder.setLength(builder.length() - 1);
            }

            builder.append("\n");

            // Recurse into children with adjusted prefix
            String newPrefix = prefix + (isLast ? "   " : "│  ");
            builder.append(child.asString(newPrefix, false));
        }

        return builder.toString();
    }



}
