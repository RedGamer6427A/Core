package dev.redgamer6427a.core.messagebus.server;

import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.function.BiConsumer;

public class BrokerProcessor implements MessageBusInterface {
    
    @Getter
    @Setter
    private BiConsumer<Message, Optional<ClientConnection>> consumer;

    /**
     * Usually only the BrokerThread touches this
     */
    @Setter
    @Getter
    private BiConsumer<Message, ClientConnection> sender;

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

    @Override
    public int sendMessage(Message message) {
        sender.accept(message, null);
        return 0;
    }
}
