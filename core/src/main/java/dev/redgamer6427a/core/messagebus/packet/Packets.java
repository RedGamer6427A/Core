package dev.redgamer6427a.core.messagebus.packet;

import com.google.gson.Gson;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusInterface;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Consumer;

public class Packets {

    private static final Logger logger = Logger.create();

    /**
     * Set this so that SerializablePacket doesn't need a client as an input. Leave as null if proxy.
     */
    @Getter
    @Setter
    private static MessageBusInterface messageBusInterface;

    @Getter
    @Setter
    private static boolean shouldUnknownError;

    private static void maybeError(String message, Object... params){
        if (shouldUnknownError) {
            logger.error(message, params);
        } else {
            logger.fine(message);
        }
    }

    public static boolean handle(Message message) {
        Packet packet = fromMessage(message);
        if (packet == null) {
            maybeError("Could not deserialize packet. Packet was null.");
            return false;
        }

        boolean result = false;
        for (Map.Entry<Class<?>, Consumer<Packet>> c : consumers) {

            if (c.getKey().isAssignableFrom(packet.getClass())) {
                try {
                    c.getValue().accept(packet);
                } catch (Exception e) {
                    logger.catching(e);
                }
                result = true;
            }
        }
        return result;

    }

    @Getter
    private static final List<Map.Entry<Class<?>, Consumer<Packet>>> consumers = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static <T extends Record & Packet> void addHandler(Class<T> type, Consumer<T> handler) {
        consumers.add(Map.entry(type, (Consumer<Packet>) (Consumer<?>) handler));
        registerPacketType(type);
    }

    private static final Gson gson = new Gson();


    @Getter
    private static final Set<Class<? extends Record>> packetTypes = new HashSet<>();




    private static final Map<String, Class<? extends Record>> packetTypesMap = new HashMap<>();


    private static String instantiateForPacketType(Class<? extends Record> clazz) {
        try {
            Object[] zeroArgs = Arrays.stream(clazz.getRecordComponents())
                    .map(rc -> zeroValue(rc.getType()))
                    .toArray();
            Class<?>[] paramTypes = Arrays.stream(clazz.getRecordComponents())
                    .map(RecordComponent::getType)
                    .toArray(Class[]::new);

            Constructor<? extends Record> ctor = clazz.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            Packet instance = (Packet) ctor.newInstance(zeroArgs);
            return instance.getPacketType();
        } catch (Exception e) {
            logger.error("Failed to register message type {}: {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    public static void registerPacketType(Class<? extends Record> packetClass) {
        packetTypes.add(packetClass);
        String type = instantiateForPacketType(packetClass);
        if (type != null) {
            packetTypesMap.put(type, packetClass);
        }
    }
    // avoid NPE on primitive unboxing when probing getMessageType() with dummy args
    private static Object zeroValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return (char) 0;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        return 0d; // double
    }

    public static <T extends Record> T fromMessage(Message message, Class<T> clazz) {
        Map<String, String> map = message.contents();

        List<Object> values = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();

        for (RecordComponent rc : clazz.getRecordComponents()) {
            String str = map.get(rc.getName());
            Object value = gson.fromJson(str, rc.getGenericType()); // was rc.getType()
            values.add(value);
            classes.add(rc.getType()); // keep raw type here, for constructor lookup
        }

        Constructor<T> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(classes.toArray(Class[]::new));
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            return constructor.newInstance(values.toArray());
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Packet fromMessage(Message message) {

        String type = message.contents().get("packetType");
        Class<? extends Record> clazz = packetTypesMap.get(type);

        if (clazz == null) {
            maybeError("Received an unknown message type ({})!", type);
            return null;
        }

        return (Packet) fromMessage(message, clazz);
    }

}
