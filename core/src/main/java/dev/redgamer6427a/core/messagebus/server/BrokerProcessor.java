package dev.redgamer6427a.core.messagebus.server;

import dev.redgamer6427a.core.messagebus.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class BrokerProcessor {
    
    @Getter
    @Setter
    private Consumer<Message> consumer;

    /**
     * Usually only the BrokerThread touches this
     */
    @Setter
    @Getter
    private Consumer<Message> sender;

    public void dispatch(Message message) {
        sender.accept(message);
    }

    public BrokerProcessor(BrokerThread brokerThread, Consumer<Message> consumer) {
        brokerThread.getBrokerProcessors().add(this);
        this.sender = brokerThread::dispatch;
        this.consumer = consumer;
    }
    
}
