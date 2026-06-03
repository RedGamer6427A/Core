package dev.redgamer6427a.admiral.paper.command.argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * This class represents a Command Argument.
 */
public class Argument {
    /**
     * The argument name. The little "<>" text above chat.
     *
     */
    private final String name;

    /**
     * The argument type. Use the default Brigadier ones or use ArgumentTypes.() for others
     *
     */
    private final ArgumentType type;

    /**
     *
     * @param name The argument name. The little "<>" text above chat.
     * @param type The argument type. Use the default Brigadier ones or use ArgumentTypes.() for others
     */
    public Argument(String name, ArgumentType type) {
        this.name = name;
        this.type = type;

    }

    /**
     *
     * @return the argument name
     */
    public String name(){
        return name;
    }
    /**
     *
     * @return the argument type
     */
    public ArgumentType type(){
        return type;
    }

    /**
     * Purely for convenience
     * @return the RequiredArgumentBuilder that would match the argument
     * @param <T>
     */
    public <T> RequiredArgumentBuilder<CommandSourceStack, T> asArgument(){
        return Commands.argument(name, type);

    }


    /**
     * This method provides a faster and more readable way of getting an argument
     * @param context The CommandContext
     * @param clazz The type you expect. Purely for convenience
     * @return The arguments value. Type is the one you inputted
     */
    @SuppressWarnings("unchecked")
    public <V> V resolve(CommandContext<CommandSourceStack> context, Class<V> clazz) {
        return context.getArgument(name, clazz);
    }
}
