package dev.redgamer6427a.core.minecraft.paper.testing.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusBrokerResponses;
import dev.redgamer6427a.core.minecraft.paper.command.AllowedSources;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import dev.redgamer6427a.core.minecraft.paper.testing.PaperTestPlugin;
import dev.redgamer6427a.core.utils.AntiSwear;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GlobalChatCommand extends BrigadierCommand {
    public GlobalChatCommand() {
        super("globalchat", "global chat", "gc");

        Argument textArg = new Argument("text", StringArgumentType.greedyString());
        addSyntax(context -> {

            String text = textArg.resolve(context, String.class);

            if (AntiSwear.checkForSwear(text)) {
                answer(context, "Please do not swear!");
                return;
            }

            Map<String, String> map = Map.of(
                    "eventType", "globalchat",
                    "sender", context.getSource().getSender().getName(),
                    "content", text
            );

            CompletableFuture<Integer> future = ((PaperTestPlugin) PaperTestPlugin.getInstance()).client.sendMessageAsync(new Message("*" , map, false));
            future.thenAccept(integer -> {
                if (integer != 0) {
                    answer(context, "<red>An exception occurred while sending this message!", "<red>An exception occurred while sending this message: <dark_red>"+ MessageBusBrokerResponses.fromCode(integer));
                }
            });

        }, AllowedSources.PLAYER, textArg);

    }
}
