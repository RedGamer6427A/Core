package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.logging.Level;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.client.MessageBusClient;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
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
        Logger.setMinLevel(Level.FINEST);
        Logger.setOut(s -> logger().info(s));
        Logger.setErrOut(s -> logger().error(s));


    }

    @Override
    public void afterEnable() {
        AdventureMM.registerHead("2012", "db0c1cd6865a533d22633a3b4e1d4c88b52bf0c669d173c143d2ba4307af9462");
        AdventureMM.registerHead("evil", "ac2fbe2b06d905710c958cd520ae7c8c6eec1205ed3a32460be24e3ae9b80d41");
        String clientID = Config.getInstance().getChatCategory().clientID.value();
        if (!Objects.equals(clientID, "none")) {
            client = new MessageBusClient("127.0.0.0", 25581, "ascii-password", clientID);
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
            client.close(true);
        } catch (Exception ignored) {
        }
    }
}
