package dev.redgamer6427a.core.minecraft.velocity.command;

import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.CommandSource;

import java.util.function.Predicate;

/**
 * This is used for the defaultExecutor
 */
public record Executor(Command<CommandSource> context, Predicate<CommandSource> requirement) {




}
