package dev.redgamer6427a.core.minecraft.velocity.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public interface SmartArgumentType<S, R> {
    /**
     * Server-side resolver for your argument (e.g. lookup a RegisteredServer from string)
     */
    R resolve(CommandContext<CommandSource> context, String input, Argument argument) throws CommandSyntaxException;

    /**
     * Tab-completion suggestions
     */
    default CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return builder.buildFuture();
    }

    ArgumentType<S> getSimple();

}

