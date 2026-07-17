package dev.redgamer6427a.core.minecraft.paper.testing.command;

import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusBrokerResponse;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.testing.PaperTestPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GlobalReloadCommand extends BrigadierCommand {
    public GlobalReloadCommand() {
        super("globalreload", "reloads all servers", "greload");

        setDefaultExecutor(context -> {

            Map<String, String> map = Map.of(
                    "eventType", "reload-all"
            );

            CompletableFuture<Integer> future = ((PaperTestPlugin) PaperTestPlugin.getInstance()).client.sendMessageAsync(new Message("*" , map, false));
            future.thenAccept(integer -> {
                if (integer != 0) {
                    answer(context, "<red>An exception occurred while sending this command!", "<red>An exception occurred while sending this command: <dark_red>"+ MessageBusBrokerResponse.fromCode(integer));
                }
            });

        });

    }
}
