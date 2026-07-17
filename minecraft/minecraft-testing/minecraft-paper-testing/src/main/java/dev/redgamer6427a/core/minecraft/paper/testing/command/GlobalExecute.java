package dev.redgamer6427a.core.minecraft.paper.testing.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import dev.redgamer6427a.core.minecraft.paper.testing.messaging.CommandBroadcastPacket;

public class GlobalExecute extends BrigadierCommand {
    public GlobalExecute() {
        super("globalexecute");

        Argument argument = new Argument("command", StringArgumentType.greedyString());

        addSyntax(context -> {
            answer("<green>Sending packet!");

            String string = argument.resolve(context, String.class);

            new CommandBroadcastPacket(string).send();

        }, argument);

    }
}
