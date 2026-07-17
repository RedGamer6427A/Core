package dev.redgamer6427a.core.minecraft.paper.testing.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusBrokerResponse;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import dev.redgamer6427a.core.minecraft.paper.testing.PaperTestPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GlobalOpCommand extends BrigadierCommand {

    public GlobalOpCommand() {
        super("globalop", "Ops a player on every server", "gop");
        Argument playerArg = new Argument("player", StringArgumentType.word());
        addSyntax(context -> {

            String player = playerArg.resolve(context, String.class);

            Map<String, String> map = Map.of(
                    "eventType", "global-op",
                    "subject", player,
                    "sender", context.getSource().getSender().getName()
            );

            CompletableFuture<Integer> future = ((PaperTestPlugin) PaperTestPlugin.getInstance()).client.sendMessageAsync(new Message("*" , map, false));
            future.thenAccept(integer -> {
                if (integer != 0) {
                    answer(context, "<red>An exception occurred while sending this command!", "<red>An exception occurred while sending this command: <dark_red>"+ MessageBusBrokerResponse.fromCode(integer));
                }
            });

        }, playerArg);
    }

}
