package dev.redgamer6427a.core.minecraft.velocity.command;

import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.CommandSource;
import dev.redgamer6427a.core.minecraft.velocity.command.argument.Argument;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * A Syntax Tree representation
 * @param arguments the structure based on arguments
 */
public record SyntaxTree(Command<CommandSource> context, Predicate<CommandSource> requirement, Collection<Argument> arguments) {

}
