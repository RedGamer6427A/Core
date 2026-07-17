package dev.redgamer6427a.core.minecraft.paper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Predicate;

/**
 * This is used for the defaultExecutor
 * @param command The code to be executed
 */
public record Executor(CommandRunner<CommandSourceStack> command, Predicate<CommandSourceStack> requirement) {




}
