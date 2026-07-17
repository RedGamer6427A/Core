package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.logging.Level;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.client.MessageBusClient;
import dev.redgamer6427a.core.messagebus.packet.Packets;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.testing.command.*;
import dev.redgamer6427a.core.minecraft.paper.testing.messaging.CommandBroadcastPacket;
import dev.redgamer6427a.core.minecraft.paper.util.PaperParameters;
import org.bukkit.Bukkit;
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
        Packets.setShouldUnknownError(true);

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
                boolean packet = Packets.handle(message);
                if (!packet) {
                    try {
                        MessagingHandler.handle(message);
                    }   catch (Exception e) {
                        logger.catching(e);
                    }
                }
            });
            Packets.setMessageBusClient(client);
            client.subscribe();
        }

        Packets.addHandler(CommandBroadcastPacket.class, commandBroadcastPacket -> {
            logger.info("Executing CommandBroadcastPacket: ", commandBroadcastPacket.command());

            Bukkit.getScheduler().callSyncMethod(this, () -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), commandBroadcastPacket.command());
                return null;
            });

        });

    }

    @Override
    public void defineCommands() {
        new TestCommand().register();
        new GlobalChatCommand().register();
        new GlobalOpCommand().register();
        new GlobalDeopCommand().register();
        new GlobalReloadCommand().register();
        new GlobalExecute().register();
        new GlobalProxyExecute().register();
    }

    @Override
    public void disable() {
        try {
            client.close(true);
        } catch (Exception ignored) {
        }
    }
}
