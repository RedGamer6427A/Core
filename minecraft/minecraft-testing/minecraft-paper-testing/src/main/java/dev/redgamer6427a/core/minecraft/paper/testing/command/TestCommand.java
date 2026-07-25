package dev.redgamer6427a.core.minecraft.paper.testing.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class TestCommand extends BrigadierCommand {
    public TestCommand() {
        super("cmd", "TEST!");

        Argument argument = new Argument("minimessage", StringArgumentType.greedyString());

        addSyntax(context -> {
            String mini = context.getArgument("minimessage", String.class);
            answer(context, AdventureMM.cc(mini));
            context.getSource().getSender().sendMessage(AdventureMM.serialize(AdventureMM.cc(mini)));
            context.getSource().getSender().sendMessage(GsonComponentSerializer.gson().serialize(AdventureMM.cc(mini)));

        }, argument);
        addSubCommand(new SubCommand());
    }
}
