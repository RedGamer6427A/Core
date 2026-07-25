package dev.redgamer6427a.core.minecraft.velocity.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;


import com.velocitypowered.api.proxy.Player;
import dev.redgamer6427a.core.console.output.ANSICode;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.velocity.VelocityPlugin;
import dev.redgamer6427a.core.minecraft.velocity.command.argument.Argument;

import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.function.Predicate;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.cc;
import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.mmToConsole;


/**
 * Represents a command wrapper around Mojang's Brigadier system.
 * Provides support for executors, syntax trees, subcommands, and requirements.
 */
public abstract class BrigadierCommand {

    private final String literal;
    private final Collection<String> aliases;
    private final String description;
    private Executor defaultExecutor;
    private final List<SyntaxTree> syntaxTrees = new ArrayList<>();
    private final List<BrigadierCommand> subCommands = new ArrayList<>();
    private static String playerRequiredMessage = "<red>A player is required to run this command here";

    /**
     * Creates a new Brigadier command definition.
     *
     * @param literal     the base literal of the command
     * @param description the description of the command
     * @param aliases     optional aliases for the command
     */
    public BrigadierCommand(String literal, String description, String... aliases) {
        this.literal = literal;
        this.description = description;
        this.aliases = Arrays.stream(aliases).toList();
    }

    /**
     * Sets the default executor for this command.
     *
     * @param command     the command to execute
     * @param requirement additional requirement predicate
     */
    public void setDefaultExecutor(Command<CommandSource> command, Predicate<CommandSource> requirement) {
        this.defaultExecutor = new Executor(command, requirement);
    }


    
    /**
     * Sets the default executor for this command without a requirement.
     *
     * @param command    the command to execute
     */
    public void setDefaultExecutor(Command<CommandSource> command) {
        this.defaultExecutor = new Executor(command, null);
    }

    /**
     * Registers this command with the command manager.
     */
    public void register() {
        BrigadierCommandManager.registerCommand(this);
    }

    /**
     * Builds the command into a Brigadier node.
     *
     * @return the built literal command node
     */
    public LiteralCommandNode<CommandSource> build() {
        return asBuilder().build();
    }

    /**
     * Converts this command into a Brigadier literal builder.
     *
     * @return the literal argument builder
     */
    public LiteralArgumentBuilder<CommandSource> asBuilder() {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(literal);
        if (defaultExecutor != null) {
            if (defaultExecutor.requirement() != null) {
                builder.requires(defaultExecutor.requirement());
            }


            builder = builder.executes(context -> defaultExecutor.context().run(context));
        }
        for (SyntaxTree syntaxTree : syntaxTrees) {
            RequiredArgumentBuilder<CommandSource, Object> e = null;
            List<Argument> invertedList = new ArrayList<>(syntaxTree.arguments().stream().toList());
            Collections.reverse(invertedList);
            int i = 0;
            for (Argument argument : invertedList) {
                i++;
                RequiredArgumentBuilder<CommandSource, Object> builderArg = argument.asArgument();

                if (i == 1) {
                    e = builderArg.executes(context -> syntaxTree.context().run(context));
                } else {
                    e = builderArg.then(e);
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
        return builder;
    }

    /**
     * Adds a command syntax with arguments.
     *
     * @param command     the executor
     * @param requirement additional requirement
     * @param arguments   the arguments of the syntax
     */
    public void addSyntax(Command<CommandSource> command, Predicate<CommandSource> requirement, Argument... arguments) {
        syntaxTrees.add(new SyntaxTree(command, requirement, Arrays.stream(arguments).toList()));
    }

    /**
     * Adds a command syntax with arguments without a requirement.
     *
     * @param command    the executor
     * @param arguments  the arguments of the syntax
     */
    public void addSyntax(Command<CommandSource> command, Argument... arguments) {
        syntaxTrees.add(new SyntaxTree(command, null, Arrays.stream(arguments).toList()));
    }

    /**
     * Adds a subcommand.
     *
     * @param brigadierCommand the subcommand
     */
    public void addSubCommand(BrigadierCommand brigadierCommand) {
        subCommands.add(brigadierCommand);
    }

    /**
     * Gets the literal name of this command.
     *
     * @return the literal
     */
    public String literal() {
        return literal;
    }

    /**
     * Gets the aliases of this command.
     *
     * @return the aliases
     */
    public Collection<String> aliases() {
        return aliases;
    }

    /**
     * Gets the description of this command.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    /**
     * Gets the default player-required message.
     *
     * @return the message
     */
    public static String playerRequiredMessage() {
        return playerRequiredMessage;
    }

    /**
     * Sets the default player-required message.
     *
     * @param message the message
     */
    public static void playerRequiredMessage(String message) {
        playerRequiredMessage = message;
    }

    /**
     * Sends a response to a command source.
     *
     * @param context the context
     * @param message the message
     */
    public static void answer(CommandContext<CommandSource> context, Component message) {
        CommandSource source = context.getSource();
        if (source instanceof ConsoleCommandSource) {
            source.sendMessage(AdventureMM.cc(mmToConsole(message) + ANSICode.RESET.format()));
        } else {
            source.sendMessage(message);
        }
    }

    /**
     * Sends a response to a command source.
     *
     * @param context the context
     * @param message the message
     */
    public static void answer(CommandContext<CommandSource> context, String message) {
        CommandSource source = context.getSource();
        if (source instanceof ConsoleCommandSource) {
            source.sendMessage(AdventureMM.cc(message + ANSICode.RESET.format()));
        } else {
            source.sendMessage(AdventureMM.cc(message));
        }
    }


    /**
     * Sends a response to a command source.
     *
     * @param context the context
     * @param message the message
     */
    public static void answer(CommandContext<CommandSource> context, Component message, Component adminMessage) {
        CommandSource source = context.getSource();

        if(context.getSource().hasPermission(VelocityPlugin.getInstance().getVerboseAnswerPermission())){
            if (source instanceof ConsoleCommandSource) {
                source.sendMessage(AdventureMM.cc(mmToConsole(adminMessage) + ANSICode.RESET.format()));
            } else {
                source.sendMessage(adminMessage);
            }

        } else {
            if (source instanceof ConsoleCommandSource) {
                source.sendMessage(AdventureMM.cc(mmToConsole(message) + ANSICode.RESET.format()));
            } else {
                source.sendMessage(message);
            }
        }



    }

    /**
     * Sends a response to a command source.
     *
     * @param context the context
     * @param message the message
     *
     */
    public static void answer(CommandContext<CommandSource> context, String message, String adminMessage) {
        answer(context, AdventureMM.cc(message), AdventureMM.cc(adminMessage));
    }

    public static String executor(CommandSource source, boolean capsFirst){
        if(source instanceof ConsoleCommandSource){
            return (capsFirst ? "T" : "t" ) + "he Console";
        } else if(source instanceof Player p){
            return p.getUsername();
        } else {
            return "Backend Console or Unknown";
        }


    }

}
