package dev.redgamer6427a.admiral.paper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface CommandRunner<S>  {
    void run(CommandContext<S> context) throws CommandSyntaxException;


}
