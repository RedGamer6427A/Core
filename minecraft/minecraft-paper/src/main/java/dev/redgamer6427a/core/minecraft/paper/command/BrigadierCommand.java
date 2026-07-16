package dev.redgamer6427a.core.minecraft.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.redgamer6427a.core.console.output.ConsoleMiniMessage;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.common.text.TerminalStyle;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.function.Predicate;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.mm;
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
     * @param allowedSources allowed command sources (e.g. player, console)
     * @param requirement    an optional Brigadier requirement predicate
     */
    public void setDefaultExecutor(CommandRunner<CommandSourceStack> command, AllowedSources allowedSources, Predicate<CommandSourceStack> requirement) {
        this.defaultExecutor = new Executor(command, allowedSources, requirement);
    }

    /**
     * Sets the default executor without a requirement.
     *
     * @param command        the executed code
     * @param allowedSources allowed command sources
     */
    public void setDefaultExecutor(CommandRunner<CommandSourceStack> command, AllowedSources allowedSources) {
        this.defaultExecutor = new Executor(command, allowedSources, null);
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

    /**
     * Internal helper to construct the Brigadier literal command node.
     *
     * @return the constructed command node
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        builder = Commands.literal(literal);
        if (defaultExecutor != null) {
            Predicate<CommandSourceStack> p = allowedSources(defaultExecutor.allowedSources());

            if (defaultExecutor.requirement() != null) {
                p = p.and(defaultExecutor.requirement());
            }

            builder.requires(p);
            builder.executes(context -> {
                defaultExecutor.command().run(context);
                return Command.SINGLE_SUCCESS;
            });

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

                    e.executes(context -> {
                        syntaxTree.context().run(context);
                        return Command.SINGLE_SUCCESS;
                    });
                } else {
                    e = argument.asArgument().then(e);
                }
            }

            if (syntaxTree.requirement() != null) {
                builder.requires(mergeRequirements(syntaxTree.requirement(), allowedSources(syntaxTree.allowedSources()))).then(e);
            } else if (defaultExecutor != null && defaultExecutor.requirement() != null) {
                builder.requires(mergeRequirements(defaultExecutor.requirement(), allowedSources(defaultExecutor.allowedSources()))).then(e);
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
     * Checks allowed command sources (player, console, command block).
     *
     * @param allowedSources the allowed sources
     */
    private static Predicate<CommandSourceStack> allowedSources(AllowedSources allowedSources) {
        return cs -> {
            CommandSender sender = cs.getSender();
            if (!allowedSources.isCanPlayer() && sender instanceof Player) return false;
            if (!allowedSources.isCanConsole() && sender instanceof ConsoleCommandSender) return false;
            if (!allowedSources.isCanCommandBlock() && sender instanceof BlockCommandSender) return false;

            return true;
        };
    }


    /**
     * Adds a possible command syntax with a requirement.
     *
     * @param command        the executed code
     * @param allowedSources allowed command sources
     * @param requirement    the Brigadier requirement predicate
     * @param arguments      the syntax structure based on arguments
     */
    public void addSyntax(CommandRunner<CommandSourceStack> command, AllowedSources allowedSources, Predicate<CommandSourceStack> requirement, Argument... arguments) {
        syntaxTrees.add(new SyntaxTree(command, allowedSources, requirement, Arrays.stream(arguments).toList()));
    }

    /**
     * Adds a possible command syntax without a requirement.
     *
     * @param command        the executed code
     * @param allowedSources allowed command sources
     * @param arguments      the syntax structure based on arguments
     */
    public void addSyntax(CommandRunner<CommandSourceStack> command, AllowedSources allowedSources, Argument... arguments) {
        syntaxTrees.add(new SyntaxTree(command, allowedSources, null, Arrays.stream(arguments).toList()));
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

    /**
     * Sends a message to the command source.
     *
     * @param context the command context
     * @param message the message component
     */
    public static void answer(CommandContext<CommandSourceStack> context, Component message) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(mmToConsole(message) + TerminalStyle.RESET);
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
            sender.sendMessage(ConsoleMiniMessage.mm(message) + TerminalStyle.RESET);
        } else {
            sender.sendMessage(mm(message));
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


        if (context.getSource().getSender().hasPermission(PaperPlugin.getInstance().getParameters().verbosePermission())) {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(AdventureMM.serialize(adminMessage) + TerminalStyle.RESET));
            } else {
                source.getSender().sendMessage(adminMessage);
            }
        } else {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(AdventureMM.serialize(message) + TerminalStyle.RESET));
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


        if (context.getSource().getSender().hasPermission(PaperPlugin.getInstance().getParameters().verbosePermission())) {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(adminMessage + TerminalStyle.RESET));
            } else {
                source.getSender().sendMessage(mm(adminMessage));
            }
        } else {
            if (source.getSender() instanceof ConsoleCommandSender) {
                source.getSender().sendMessage(ConsoleMiniMessage.mm(message + TerminalStyle.RESET));
            } else {
                source.getSender().sendMessage(mm(message));
            }
        }
    }


    protected static Predicate<CommandSourceStack> collectRequirements(BrigadierCommand command) {
        List<Predicate<CommandSourceStack>> predicates = new ArrayList<>();

        if (command.defaultExecutor != null && command.defaultExecutor.requirement() != null) {
            predicates.add(command.defaultExecutor.requirement().and(allowedSources(command.defaultExecutor.allowedSources())));
        }

        command.syntaxTrees.forEach(syntaxTree -> {

            if (syntaxTree.requirement() != null) {
                predicates.add(syntaxTree.requirement().and(allowedSources(syntaxTree.allowedSources())));
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
