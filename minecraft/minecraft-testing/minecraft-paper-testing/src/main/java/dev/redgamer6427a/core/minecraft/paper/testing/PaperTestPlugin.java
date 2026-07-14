package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.client.MessageBusClient;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.testing.command.*;
import dev.redgamer6427a.core.minecraft.paper.util.PaperParameters;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class PaperTestPlugin extends PaperPlugin {

    private static final Logger logger = Logger.create();


    @Override
    public @NonNull PaperParameters getParameters() {
        return new PaperParameters(new Permission("papertest.verbose"), null, false, false);
    }

    public MessageBusClient client;

    @Override
    public void defineConfigurations() {
        Config.getInstance();
    }

    @Override
    public void beforeEnable() {

        Logger.setOut(s -> logger().info(s));
        Logger.setErrOut(s -> logger().error(s));


    }

    @Override
    public void afterEnable() {
        String clientID = Config.getInstance().getChatCategory().clientID.value();
        if (!Objects.equals(clientID, "none")) {
            client = new MessageBusClient("127.0.0.0", 12443, "just_a_pass", clientID);
            client.onMessage(message -> {
                logger().info(message.toString());
                MessagingHandler.handle(message);
            });

            client.subscribe();
        }

    }

    @Override
    public void defineCommands() {
        new TestCommand().register();
        new GlobalChatCommand().register();
        new GlobalOpCommand().register();
        new GlobalDeopCommand().register();
        new GlobalReloadCommand().register();
    }

    @Override
    public void disable() {
        try {
            client.close();
        } catch (Exception ignored) {
        }
    }
}
