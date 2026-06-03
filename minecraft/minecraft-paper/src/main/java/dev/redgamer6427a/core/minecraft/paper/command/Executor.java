package dev.redgamer6427a.admiral.paper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Predicate;

/**
 * This is used for the defaultExecutor
 * @param command The code to be executed
 * @param allowedSources whether it should be Player only
 */
public record Executor(CommandRunner<CommandSourceStack> command, AllowedSources allowedSources, Predicate<CommandSourceStack> requirement) {




}
