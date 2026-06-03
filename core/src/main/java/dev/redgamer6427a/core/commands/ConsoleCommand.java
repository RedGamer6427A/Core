package dev.redgamer6427a.core.commands;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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


    protected ConsoleCommand(String name, Consumer<CommandSyntaxException> syntaxExceptionConsumer) {
        super(null, null, name);
        this.syntaxExceptionConsumer = syntaxExceptionConsumer;
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

//            if (!node.getArguments().isEmpty()) {
//                throw new IllegalStateException("Node has more than one argument tree");
//            }

            last.addChild(node);
            last = node;


        }

        LiteralCommandNode finalLast = last;
        int finalI = i;

        List<ArgumentNode<?>> processed = List.of(arguments);

        List<ArgumentNode<?>> finalProcessed = processed;

        LiteralCommandNode finalLast1 = last;
        processed = processed.stream()
                .filter(node -> {
                    int index = finalProcessed.indexOf(node);
                    if (finalLast1.getArguments().size() <= index) {
                        return true;
                    }
                    if (index < 0) {
                        return true;
                    }
                    return !node.getName().equals(finalLast1.getArguments().get(index).getName());
                })
                .toList();

        last.getArguments().addAll(processed.stream().peek(argumentNode -> {
            argumentNode.setParent(finalLast);
            argumentNode.setDepth(finalI);
        }).toList());
        processed.stream().toList().getLast().setExecutor(executor);

    }

    /**
     * Add a syntax tree possibility.
     * @param executor the executor to be used if the user uses this exact route.
     * @param arguments the arguments.
     */
    protected void addSyntax(CommandExecutor executor, ArgumentNode<?>... arguments) {
        List<ArgumentNode<?>> processed = List.of(arguments);

        List<ArgumentNode<?>> finalProcessed = processed;

        processed = processed.stream()
                .filter(node -> {
                    int index = finalProcessed.indexOf(node);
                    if (getArguments().size() <= index) {
                        return true;
                    }
                    if (index < 0) {
                        return true;
                    }
                    return !node.getName().equals(getArguments().get(index).getName());
                })
                .toList();


        getArguments().addAll(processed.stream().peek(argumentNode -> {
            argumentNode.setParent(this);
            argumentNode.setDepth(0);
        }).toList());

        Arrays.stream(arguments).toList().getLast().setExecutor(executor);

    }

    /**
     * Add a syntax tree possibility.
     * @param executor the executor to be used if the user uses this exact route.
     * @param literals the literals or 'subcommands'.
     */
    protected void addSyntax(CommandExecutor executor, String literals) {
        String[] stringLiterals = literals.split(" ");

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

        List<String> args = Arrays.stream(s.strip().split(" ")).collect(Collectors.toList());

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
                for (int i = iSpecial; i < args.size();) {

                    if (relIndex >= last.getArguments().size()) {
                        invalidSyntaxError();
                        return true;
                    }

                    ArgumentNode<?> node = last.getArguments().get(relIndex);

                    relIndex++;

                    try {
                        int generatedOffset = context.parse(node).generatedOffset();

                        i += generatedOffset;
                    } catch (CommandSyntaxException e) {
                        syntaxExceptionConsumer.accept(e);
                        break;
                    }


                    if (i - 1 == args.size() - 1) {
                        if (node.getExecutor() != null) {
                            handleExecutor(context, node.getExecutor());
                        } else {
                            invalidSyntaxError();
                            return true;

                        }
                        break;
                    }


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
