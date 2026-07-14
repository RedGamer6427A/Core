package dev.redgamer6427a.core.minecraft.velocity.testing;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.server.BrokerProcessor;
import dev.redgamer6427a.core.messagebus.server.BrokerThread;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;

public class VelocityTest {

    @Inject
    private Logger logger;

    BrokerThread brokerThread;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent even) {
        logger.info("Core Velocity Testing Plugin Initialized.");
        brokerThread = new BrokerThread(12443, "just_a_pass");
        new Thread(brokerThread).start();
        BrokerProcessor brokerProcessor = new BrokerProcessor(brokerThread, message -> {
            logger.info("Message: {}", message);

        });

        brokerProcessor.setConsumer(message -> {
            logger.info("Message: {}", message);
            if (!Objects.equals(message.sender(), "broker")) {
                brokerProcessor.dispatch(new Message("*", "broker", Map.of("message", "recieved"), false));
            }
        });

    }
}
