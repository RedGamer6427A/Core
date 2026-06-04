package dev.redgamer6427a.core.minecraft.paper.command;

import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * A Syntax Tree representation
 * @param context the code to be executed
 * @param allowedSources whether the context should be player only
 * @param arguments the structure based on arguments
 */
public record SyntaxTree(CommandRunner<CommandSourceStack> context, AllowedSources allowedSources, Predicate<CommandSourceStack> requirement, Collection<Argument> arguments) {

}
