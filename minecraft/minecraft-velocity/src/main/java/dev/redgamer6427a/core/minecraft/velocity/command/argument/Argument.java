package dev.redgamer6427a.core.minecraft.velocity.command.argument;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.velocitypowered.api.command.CommandSource;
import lombok.Getter;

/**
 * This class represents a Command Argument.
 */
@Getter
public class Argument {
    /**
     * The argument name. The little "<>" text above chat.
     */
    private final String name;


    /**
     * The argument type. Use the default Brigadier ones or use ArgumentTypes.() for others
     */
    private final ArgumentType sType;
    private final SmartArgumentType cType;

    private final Complexity complexity;
    /**
     * @param name The argument name. The little "<>" text above chat.
     * @param type The argument type. Use the default Brigadier ones or use ArgumentTypes.() for others
     */
    public Argument(String name, ArgumentType type) {
        this.name = name;
        this.sType = type;
        this.cType = null;
        this.complexity = Complexity.SIMPLE;

    }

    public Argument(String name, SmartArgumentType type) {
        this.name = name;
        this.sType = type.getSimple();
        this.cType = type;
        this.complexity = Complexity.COMPLEX;
    }




    /**
     * Purely for convenience
     *
     * @param <T>
     * @return the RequiredArgumentBuilder that would match the argument
     */
    @SuppressWarnings("unchecked")
    public <T> RequiredArgumentBuilder<CommandSource, T> asArgument() {
        if(complexity == Complexity.COMPLEX) {
            return RequiredArgumentBuilder.<CommandSource, T>argument(name, sType).suggests((context, builder) -> cType.suggest(context, builder));
        }
        return RequiredArgumentBuilder.<CommandSource, T>argument(name, sType);
    }


    @SuppressWarnings("unchecked")
    public <V> V resolve(CommandContext<CommandSource> context, Class<V> clazz) throws CommandSyntaxException {
        if (complexity == Complexity.COMPLEX) {
            String input = context.getArgument(name, String.class);
            return (V) cType.resolve(context, input, this);
        }
        return context.getArgument(name, (Class<V>) Object.class);
    }

    public enum Complexity{
        SIMPLE,
        COMPLEX;


    }


}
