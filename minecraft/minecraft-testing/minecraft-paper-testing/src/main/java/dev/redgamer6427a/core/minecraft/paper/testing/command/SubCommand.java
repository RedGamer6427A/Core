package dev.redgamer6427a.core.minecraft.paper.testing.command;

import ca.spottedleaf.concurrentutil.completable.Completable;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.minecraft.paper.command.AllowedSources;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.testing.PaperTestPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SubCommand extends BrigadierCommand {


    public SubCommand() {
        super("sub", "i should lk remove this");
        setDefaultExecutor(context -> {

            Map<String, String> map = Map.of("test", "ing", "node", "1");

            CompletableFuture<Integer> future = ((PaperTestPlugin) PaperTestPlugin.getInstance()).client.sendMessageAsync(new Message("*", "node1", map, true));
            future.thenAccept(integer -> {
                answer(context, "<green>Response: <dark_green>"+integer);
            });
            }, AllowedSources.ALL);
    }

}
