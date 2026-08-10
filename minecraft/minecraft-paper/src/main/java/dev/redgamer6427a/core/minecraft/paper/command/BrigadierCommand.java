package dev.redgamer6427a.core.minecraft.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.redgamer6427a.core.console.output.ANSICode;
import dev.redgamer6427a.core.console.output.ConsoleMiniMessage;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.function.Predicate;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.mmToConsole;


/**
 * Represents a Brigadier-based command definition within the Admiral Paper system.
 * Provides command registration, syntax management, and source validation.
 */
@Getter
public abstract class BrigadierCommand {


    /**
     * The base literal (name) of the command.
     */
    private final String literal;

    /**
     * The command’s aliases (alternative names).
     */
    private final Collection<String> aliases;

    /**
     * The command’s /help description.
     */
    private final String description;

    /**
     * The Brigadier command tree builder for this command.
     */
    private LiteralArgumentBuilder<CommandSourceStack> builder;

    private LiteralCommandNode<CommandSourceStack> node;

    /**
     * The default executor for this command.
     */
    private Executor defaultExecutor;

    /**
     * A list of defined syntax trees (argument-based command structures).
     */
    private final List<SyntaxTree> syntaxTrees = new ArrayList<>();

    /**
     * A list of subcommands attached to this command.
     */
    private final List<BrigadierCommand> subCommands = new ArrayList<>();

    /**
     * Constructs a new BrigadierCommand instance.
     *
     * @param literal     the name of the command
     * @param description the /help description
     * @param aliases     the command’s aliases (alternative names)
     */
    public BrigadierCommand(String literal, String description, String... aliases) {
        this.literal = literal;
        this.description = description;
        this.aliases = Arrays.stream(aliases).toList();
    }

    /**
     * Constructs a new BrigadierCommand instance.
     *
     * @param literal     the name of the command
     */
    public BrigadierCommand(String literal) {
        this.literal = literal;
        this.description = "";
        this.aliases = List.of();
    }

    /**
     * Sets the default executor.
     *
     * @param command        the executed code
     * @param requirement    an optional Brigadier requirement predicate
     */
    public void setDefaultExecutor(CommandRunner<CommandSourceStack> command, Predicate<CommandSourceStack> requirement) {
        this.defaultExecutor = new Executor(command, requirement);
    }

    /**
     * Sets the default executor without a requirement.
     *
     * @param command        the executed code
     */
    public void setDefaultExecutor(CommandRunner<CommandSourceStack> command) {
        this.defaultExecutor = new Executor(command, null);
    }

    /**
     * Registers this command with the Brigadier command manager.
     * Should be called at the end of initialization or during plugin enable.
     * Do not use for subcommands.
     */
    public void register() {
        build();
        BrigadierCommandManager.queueCommandRegistration(this);
    }

    private static final ThreadLocal<CommandContext<CommandSourceStack>> CURRENT_CONTEXT = new ThreadLocal<>();

    private static int runWithContext(CommandContext<CommandSourceStack> context, CommandRunner<CommandSourceStack> runner) throws CommandSyntaxException {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Command executor ran off main thread — this should be impossible under normal dispatch.");
        }
        CommandContext<CommandSourceStack> previous = CURRENT_CONTEXT.get();
        CURRENT_CONTEXT.set(context);
        try {
            runner.run(context);
            return Command.SINGLE_SUCCESS;
        } finally {
            CURRENT_CONTEXT.set(previous);
        }
    }

    /**
     * Internal helper to construct the Brigadier literal command node.
     *
     * @return the constructed command node
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        builder = Commands.literal(literal);
        if (defaultExecutor != null) {


            if (defaultExecutor.requirement() != null) {
                builder.requires(defaultExecutor.requirement());
            }


            builder.executes(context -> runWithContext(context, defaultExecutor.command()));

        } else {
            builder.requires(collectRequirements(this));

        }

        for (SyntaxTree syntaxTree : syntaxTrees) {
            RequiredArgumentBuilder<CommandSourceStack, Object> e = null;
            List<Argument> arguments = new ArrayList<>(syntaxTree.arguments().stream().toList());
            Collections.reverse(arguments);

            int i = 0;
            for (Argument argument : arguments) {
                i++;
                if (i == 1) {
                    e = argument.asArgument();

                    e.executes(context -> runWithContext(context, syntaxTree.context()));
                } else {
                    e = argument.asArgument().then(e);
                }
            }

            if (syntaxTree.requirement() != null) {
                builder.requires(syntaxTree.requirement()).then(e);
            } else if (defaultExecutor != null && defaultExecutor.requirement() != null) {
                builder.requires(defaultExecutor.requirement()).then(e);
            } else {
                builder.then(e);
            }
        }

        for (BrigadierCommand subCommand : subCommands) {
            builder.then(subCommand.build());
        }

        node = builder.build();

        return node;
    }


    /**
     * Adds a possible command syntax with a requirement.
     *
     * @param command        the executed code
     * @param requirement    the Brigadier requirement predicate
     * @param arguments      the syntax structure based on arguments
     */
    public void addSyntax(CommandRunner<CommandSourceStack> command, Predicate<CommandSourceStack> requirement, Argument... arguments) {
        syntaxTrees.add(new SyntaxTree(command, requirement, Arrays.stream(arguments).toList()));
    }

    /**
     * Adds a possible command syntax without a requirement.
     *
     * @param command        the executed code
     * @param arguments      the syntax structure based on arguments
     */
    public void addSyntax(CommandRunner<CommandSourceStack> command, Argument... arguments) {
        syntaxTrees.add(new SyntaxTree(command,null, Arrays.stream(arguments).toList()));
    }

    /**
     * Adds a subcommand to this command.
     *
     * @param brigadierCommand the subcommand
     */
    public void addSubCommand(BrigadierCommand brigadierCommand) {
        subCommands.add(brigadierCommand);
    }

    // ------------------ Static utility methods ------------------
    // ok these are not THAT static lol
    public static void answer(Component message) {
        answer(requireContext(), message);
    }

    public static void answer(String message) {
        answer(requireContext(), message);
    }

    public static void answer(Component message, Component adminMessage) {
        answer(requireContext(), message, adminMessage);
    }

    public static void answer(String message, String adminMessage) {
        answer(requireContext(), message, adminMessage);
    }

    private static CommandContext<CommandSourceStack> requireContext() {
        CommandContext<CommandSourceStack> ctx = CURRENT_CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException("answer() called outside command execution context");
        }
        return ctx;
    }

    /**
     * Sends a message to the command source.
     *
     * @param context the command context
     * @param message the message component
     */
    public static void answer(CommandContext<CommandSourceStack> context, Component message) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(mmToConsole(message) + ANSICode.RESET.format());
        } else {
            sender.sendMessage(message);
        }
    }

    /**
     * Sends a message to the command source.
     *
     * @param context the command context
     * @param message the serialized message
     */
    public static void answer(CommandContext<CommandSourceStack> context, String message) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ConsoleMiniMessage.mm(message) + ANSICode.RESET.format());
        } else {
            sender.sendMessage(AdventureMM.cc(message));
        }
    }


    /**
     * Sends a response message to the command source,
     * choosing between normal and admin messages depending on permission.
     *
     * @param context      the command context
     * @param message      the user-visible message
     * @param adminMessage the developer/admin message
     */
    public static void answer(CommandContext<CommandSourceStack> context, Component message, Component adminMessage) {
        CommandSourceStack source = context.getSource();


        if (context.getSource().getSender().hasPermission(PaperPlugin.getInstance().getVerboseAnswerPermission())) {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(AdventureMM.serialize(adminMessage) + ANSICode.RESET.format()));
            } else {
                source.getSender().sendMessage(adminMessage);
            }
        } else {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(AdventureMM.serialize(message) + ANSICode.RESET.format()));
            } else {
                source.getSender().sendMessage(message);
            }
        }
    }

    /**
     * Sends a response message to the command source,
     * choosing between normal and admin messages depending on permission.
     *
     * @param context      the command context
     * @param message      the user-visible message
     * @param adminMessage the developer/admin message
     */
    public static void answer(CommandContext<CommandSourceStack> context, String message, String adminMessage) {
        CommandSourceStack source = context.getSource();


        if (context.getSource().getSender().hasPermission(PaperPlugin.getInstance().getVerboseAnswerPermission())) {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(adminMessage + ANSICode.RESET.format()));
            } else {
                source.getSender().sendMessage(AdventureMM.cc(adminMessage));
            }
        } else {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(message + ANSICode.RESET.format()));
            } else {
                source.getSender().sendMessage(AdventureMM.cc(message));
            }
        }
    }


    protected static Predicate<CommandSourceStack> collectRequirements(BrigadierCommand command) {
        List<Predicate<CommandSourceStack>> predicates = new ArrayList<>();

        if (command.defaultExecutor != null && command.defaultExecutor.requirement() != null) {
            predicates.add(command.defaultExecutor.requirement());
        }

        command.syntaxTrees.forEach(syntaxTree -> {

            if (syntaxTree.requirement() != null) {
                predicates.add(syntaxTree.requirement());
            }

        });

        command.subCommands.forEach(subCommand -> {
            predicates.add(collectRequirements(subCommand));
        });

        return commandSourceStack -> {
            if (predicates.isEmpty()) return true;

            for (Predicate<CommandSourceStack> predicate : predicates) {
                if (predicate.test(commandSourceStack)) {
                    return true;
                }
            }
            return false;
        };
    }

    @SafeVarargs
    protected static Predicate<CommandSourceStack> mergeRequirements(Predicate<CommandSourceStack>... requirements) {

        if (requirements == null || requirements.length == 0)
            throw new IllegalArgumentException("requirements may not be empty.");

        List<Predicate<CommandSourceStack>> predicates = Arrays.stream(requirements).toList();

        Predicate<CommandSourceStack> merged = predicates.getFirst();

        for (Predicate<CommandSourceStack> predicate : predicates) {

            if (predicate == null) continue;

            merged = merged.and(predicate);
        }

        return merged;

    }

    public static Predicate<CommandSourceStack> permissionPredicate(String permission) {
        return commandSourceStack -> commandSourceStack.getSender().hasPermission(permission);
    }

    public static Predicate<CommandSourceStack> permissionPredicate(Permission permission) {
        return commandSourceStack -> commandSourceStack.getSender().hasPermission(permission);
    }

}
