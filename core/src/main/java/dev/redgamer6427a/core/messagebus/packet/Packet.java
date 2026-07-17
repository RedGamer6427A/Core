package dev.redgamer6427a.core.messagebus.packet;

import com.google.gson.Gson;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusBrokerResponse;
import dev.redgamer6427a.core.messagebus.client.MessageBusClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Packet {

    String getPacketType();

    String defaultDestination();

    Boolean defaultUrgent();

    Gson gson = new Gson();

    default Message asMessage(String destination, boolean urgent) {

        if (!(this instanceof Record record)) {
            throw new IllegalStateException("Packet only supports Records!");
        }


        Map<String, String> map = new HashMap<>();

        for (RecordComponent rc : record.getClass().getRecordComponents()) {
            try {
                map.put(rc.getName(), gson.toJson(rc.getAccessor().invoke(record)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        map.put("packetType", getPacketType());

        return new Message(destination, map, urgent);
    }


    /**
     * Async btw
     * @param destination
     * @param client
     * @return
     */
    default CompletableFuture<MessageBusBrokerResponse> send(String destination, boolean urgent, MessageBusClient client) {
        Message message = asMessage(destination, urgent);

        CompletableFuture<Integer> intFuture = client.sendMessageAsync(message);
        CompletableFuture<MessageBusBrokerResponse> future = new CompletableFuture<>();
        intFuture.thenAccept(integer -> {
            MessageBusBrokerResponse messageBusBrokerResponse = MessageBusBrokerResponse.fromCode(integer);
            future.complete(messageBusBrokerResponse);
        });
        return future;


    }

    /**
     * Only use this if you've set the messageBusClient value in SerializablePackets. WILL FAIL OTHERWISE
     * @param destination
     * @return
     */
    default CompletableFuture<MessageBusBrokerResponse> send(String destination, boolean urgent) {

        MessageBusClient messageBusClient = Packets.getMessageBusClient();
        if (messageBusClient == null) {
            throw new NullPointerException("MessageBusClient is null! Please set the value in "+ Packets.class.getSimpleName()+"*!");
        }
        return send(destination, urgent, messageBusClient);

    }

    default CompletableFuture<MessageBusBrokerResponse> send() {

        MessageBusClient messageBusClient = Packets.getMessageBusClient();

        Boolean urgent = defaultUrgent();
        String destination = defaultDestination();

        if (urgent == null || destination == null) {
            throw new NullPointerException("Urgent or destination is null!");

        }
        return send(destination, urgent);
    }
}
