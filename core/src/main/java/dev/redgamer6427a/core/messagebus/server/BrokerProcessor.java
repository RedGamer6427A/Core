package dev.redgamer6427a.core.messagebus.server;

import dev.redgamer6427a.core.messagebus.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BrokerProcessor {
    
    @Getter
    @Setter
    private BiConsumer<Message, Optional<ClientConnection>> consumer;

    /**
     * Usually only the BrokerThread touches this
     */
    @Setter
    @Getter
    private BiConsumer<Message, ClientConnection> sender;

    public void dispatch(Message message) {
        sender.accept(message, null);
    }

    /**
     *
     * @param brokerThread the BrokerThread to attach to
     * @param consumer the client connection is empty if this message comes from the broker
     */
    public BrokerProcessor(BrokerThread brokerThread, BiConsumer<Message, Optional<ClientConnection>> consumer) {
        brokerThread.getBrokerProcessors().add(this);
        this.sender = brokerThread::dispatch;
        this.consumer = consumer;
    }
    
}
