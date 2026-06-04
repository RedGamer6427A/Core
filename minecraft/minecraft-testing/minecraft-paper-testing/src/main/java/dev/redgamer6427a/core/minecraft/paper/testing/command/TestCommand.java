package dev.redgamer6427a.core.minecraft.paper.testing.command;

import dev.redgamer6427a.core.minecraft.paper.command.AllowedSources;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;

public class TestCommand extends BrigadierCommand {
    public TestCommand() {
        super("cmd", "TEST!");
        setDefaultExecutor(context -> {
             answer(context, "<green>Ayyy");
        }, AllowedSources.ALL);
    }
}
