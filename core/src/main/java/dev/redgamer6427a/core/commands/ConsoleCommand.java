package dev.redgamer6427a.core.commands;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Abstract class to be used by the program.
 */
@Getter
public abstract class ConsoleCommand extends LiteralCommandNode {

    private @Nullable CommandExecutor executor;
    /**
     * An generic invalid syntax message.
     */
    public static final String invalidSyntaxMessage = "Invalid or Unknown command.";
    /**
     * This is used to determine what to do when the command throws a SyntaxException.
     */
    @Setter
    private Consumer<CommandSyntaxException> syntaxExceptionConsumer;


    protected ConsoleCommand(String name, @Nullable Consumer<CommandSyntaxException> syntaxExceptionConsumer) {
        super(null, null, name);
        this.syntaxExceptionConsumer = syntaxExceptionConsumer;
    }

    // BUG FIX (framework): builds a wildcard-typed binding without fighting generic
    // wildcard-capture in the caller. Depth is per-registration; see ArgumentBinding.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ArgumentBinding<?> bind(ArgumentNode<?> node, int depth) {
        return new ArgumentBinding(node, depth);
    }

    /**
     * Add a syntax tree possibility.
     * @param executor the executor to be used if the user uses this exact route.
     * @param literals the literals or 'subcommands' before the arguments are provided.
     * @param arguments the arguments after the literals.
     */
    protected void addSyntax(CommandExecutor executor, String literals, ArgumentNode<?>... arguments) {
        String[] stringLiterals = Arrays.stream(literals.split("\\s+")).filter(s -> !s.isEmpty()).toArray(String[]::new);

        LiteralCommandNode last = this;
        int i = 0;
        for (String name : stringLiterals) {

            i++;
            LiteralCommandNode node = last.getChildren().get(name);
            if (node == null) {
                node = new LiteralCommandNode(last, null, name);
            }

            last.addChild(node);
            last = node;
        }

        // BUG FIX (framework, root cause): arguments used to be mutated in place
        // (argumentNode.setDepth(...), argumentNode.setExecutor(...)). Since the same
        // ArgumentNode instance is routinely reused across multiple addSyntax(...) calls
        // (e.g. "id" shared by "start" and "quickstart"), a later registration silently
        // overwrote an earlier one's executor -> wrong lambda fired -> corrupted index
        // math (the ArrayIndexOutOfBoundsException / "Expected more input" crash chain).
        // Fix: wrap each argument in a fresh ArgumentBinding owned by THIS registration.
        int finalI = i;
        List<ArgumentBinding<?>> bindings = Arrays.stream(arguments)
                .map(node -> bind(node, finalI))
                .collect(Collectors.toList());

        // De-dupe against whatever's already registered on this literal node at the same
        // position + name (mirrors prior behavior, without relying on List#indexOf against
        // ArgumentNode#equals, which breaks when two args in the same call share a name).
        List<ArgumentBinding<?>> existing = last.getArguments();
        List<ArgumentBinding<?>> toAdd = new ArrayList<>();
        for (int idx = 0; idx < bindings.size(); idx++) {
            ArgumentBinding<?> b = bindings.get(idx);
            boolean duplicate = idx < existing.size()
                    && existing.get(idx).getNode().getName().equals(b.getNode().getName());
            if (!duplicate) {
                toAdd.add(b);
            }
        }

        last.getArguments().addAll(toAdd);

        bindings.get(bindings.size() - 1).setExecutor(executor);
    }

    /**
     * Add a syntax tree possibility.
     * @param executor the executor to be used if the user uses this exact route.
     * @param arguments the arguments.
     */
    protected void addSyntax(CommandExecutor executor, ArgumentNode<?>... arguments) {
        List<ArgumentBinding<?>> bindings = Arrays.stream(arguments)
                .map(node -> bind(node, 0))
                .collect(Collectors.toList());

        List<ArgumentBinding<?>> existing = getArguments();
        List<ArgumentBinding<?>> toAdd = new ArrayList<>();
        for (int idx = 0; idx < bindings.size(); idx++) {
            ArgumentBinding<?> b = bindings.get(idx);
            boolean duplicate = idx < existing.size()
                    && existing.get(idx).getNode().getName().equals(b.getNode().getName());
            if (!duplicate) {
                toAdd.add(b);
            }
        }

        getArguments().addAll(toAdd);
        bindings.get(bindings.size() - 1).setExecutor(executor);
    }

    /**
     * Add a syntax tree possibility.
     * @param executor the executor to be used if the user uses this exact route.
     * @param literals the literals or 'subcommands'.
     */
    protected void addSyntax(CommandExecutor executor, String literals) {
        String[] stringLiterals = Arrays.stream(literals.split("\\s+")).filter(s -> !s.isEmpty()).toArray(String[]::new);

        LiteralCommandNode last = this;

        for (String name : stringLiterals) {
            LiteralCommandNode node = last.getChildren().get(name);
            if (node == null) {
                node = new LiteralCommandNode(last, null, name);
            }

            last.addChild(node);
            last = node;
        }
        if (last.executor != null) throw new IllegalStateException("Executor already assigned for syntax: " + literals);
        last.executor = executor;
    }
    /**
     * Called when a user does not provide any literals or subcommands.
     * @param executor the executor to be used if the user uses no route.
     */
    protected void defaultExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public String toString() {
        return super.asString("", true);
    }

    /**
     * Accept a string as a command to be parsed.
     *
     * @param s The string to parse.
     * @return whether the command matches the defined command name.
     */
    public boolean accept(@NotNull String s) {

        List<String> args = Arrays.stream(s.strip().split("\\s+"))
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());

        ExecutionContext context = new ExecutionContext(args);

        if (args.isEmpty()) {
            throw new IllegalArgumentException("A handler executed the command without any content.");
        }

        if (!args.getFirst().equalsIgnoreCase(getName())) {
            return false;
        }


        if (args.size() == 1) {
            if (executor != null) {
                handleExecutor(context, executor);
            } else {
                invalidSyntaxError();
                return true;
            }
        } else {
            LiteralCommandNode last = this;
            int iSpecial = 1;
            for (; iSpecial < args.size(); iSpecial++) {
                if (last.getChildren().get(args.get(iSpecial)) != null) {
                    last = last.getChildren().get(args.get(iSpecial));

                } else {
                    if (last.getExecutor() == null && last.getArguments().isEmpty()) {
                        invalidSyntaxError();
                        return true;

                    }
                    break;
                }
            }


            if (iSpecial == args.size()) {
                if (last.getExecutor() != null) {
                    handleExecutor(context, last.getExecutor());
                } else {
                    invalidSyntaxError();
                    return true;
                }
            } else if (!last.getArguments().isEmpty()) {
                int relIndex = 0;
                boolean matched = false;
                for (int i = iSpecial; i < args.size();) {

                    if (relIndex >= last.getArguments().size()) {
                        invalidSyntaxError();
                        return true;
                    }

                    ArgumentBinding<?> binding = last.getArguments().get(relIndex);

                    relIndex++;

                    try {
                        int generatedOffset = context.parse(binding).generatedOffset();

                        i += generatedOffset;
                    } catch (CommandSyntaxException e) {
                        syntaxExceptionConsumer.accept(e);
                        matched = true;
                        break;
                    }


                    if (i - 1 == args.size() - 1) {
                        matched = true;
                        if (binding.getExecutor() != null) {
                            handleExecutor(context, binding.getExecutor());
                        } else {
                            invalidSyntaxError();
                            return true;

                        }
                        break;
                    }


                }

                if (!matched) {
                    invalidSyntaxError();
                    return true;
                }

            } else {
                invalidSyntaxError();
                return true;
            }
        }

        return true;
    }

    private void invalidSyntaxError() {
        syntaxExceptionConsumer.accept(new CommandSyntaxException(invalidSyntaxMessage));
    }

    protected void handleExecutor(ExecutionContext context, CommandExecutor executor) {
        try {
            executor.execute(context);
        } catch (CommandSyntaxException e) {
            syntaxExceptionConsumer.accept(e);
        }
    }

}
