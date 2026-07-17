package dev.redgamer6427a.core.minecraft.velocity.testing;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.redgamer6427a.core.messagebus.packet.Packets;
import dev.redgamer6427a.core.messagebus.server.BrokerProcessor;
import dev.redgamer6427a.core.messagebus.server.BrokerThread;
import dev.redgamer6427a.core.minecraft.velocity.VelocityPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class VelocityTest extends VelocityPlugin {

    @Inject
    private Logger logger;

    BrokerThread brokerThread;

    @Inject
    public VelocityTest(ProxyServer proxyServer, java.util.logging.Logger javaLogger, ComponentLogger componentLogger, @DataDirectory Path dataDirectory, PluginDescription pluginDescription, PluginContainer pluginContainer, ExecutorService executorService) {
        super(proxyServer, javaLogger, componentLogger, dataDirectory, pluginDescription, pluginContainer, executorService);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent even) {

        logger.info("Core Velocity Testing Plugin Initialized.");
        brokerThread = new BrokerThread(25581, "ascii-password");
        new Thread(brokerThread).start();

        Packets.addHandler(ProxyExecuteCommandPacket.class,proxyExecuteCommandPacket -> {
            proxyServer.getCommandManager().executeAsync(proxyServer.getConsoleCommandSource(), proxyExecuteCommandPacket.command());
        });

        BrokerProcessor brokerProcessor = new BrokerProcessor(brokerThread, (message, clientConnection) -> {
            logger.info("Message: {}", message);
            if (message.destination().equals("broker")){
                Packets.handle(message);
            }
        });



        // i made it shut tf up
//        brokerProcessor.setConsumer((message, clientConnection) -> {
//            logger.info("Message: {}", message);
//            if (!Objects.equals(message.sender(), "broker")) {
//                brokerProcessor.dispatch(new Message("*", "broker", Map.of("message", "recieved"), false));
//            }
//        });

    }

    @Override
    public String getVerboseAnswerPermission() {
        return "q1092313oifiughj";
    }
}
