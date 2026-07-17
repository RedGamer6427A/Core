package dev.redgamer6427a.core.messagebus.client;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusBrokerResponses;
import dev.redgamer6427a.core.messagebus.MessageBusConstants;
import dev.redgamer6427a.core.messagebus.MessageBusUtil;
import lombok.Getter;

import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.redgamer6427a.core.messagebus.MessageBusConstants.*;

public class MessageBusClient {

    private final String host;
    private final int port;
    private final String pass;
    private String clientID;

    @Getter
    private long lastHeartbeat = 0;

    @Getter
    private volatile boolean shouldBeSubscribed = false;

    private SSLSocket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread readerThread;

    // one slot: sendMessage always waits for its own reply before another can start (see sendLock)
    private final BlockingQueue<byte[]> pendingResponse = new ArrayBlockingQueue<>(1);
    private final Object sendLock = new Object(); // serializes sendMessage calls across threads

    private volatile Consumer<Message> messageHandler = msg -> {
    }; // no-op default

    private static final Logger logger = Logger.create();
    private final Gson gson = new Gson();
    private Thread heartbeatThread;

    public MessageBusClient(String host, int port, String pass, String clientID) {
        this.clientID = clientID;
        this.host = host;
        this.port = port;
        if (pass.getBytes(StandardCharsets.US_ASCII).length > PASS_LENGTH)
            throw new IllegalArgumentException("Pass length is greater than PASS_LENGTH");
        if (clientID.getBytes(StandardCharsets.US_ASCII).length > ID_LENGTH)
            throw new IllegalArgumentException("ClientID length is greater than ID_LENGTH");
        this.pass = pass;

    }

    /**
     * Register a handler for messages pushed by the broker (destination matched this client).
     */
    public void onMessage(Consumer<Message> handler) {
        this.messageHandler = handler;
    }

    public void connect() throws IOException, GeneralSecurityException {
        logger.info("Connecting to broker...");
        SSLContext ctx = SSLContext.getInstance("TLS");
        TrustManager trustAll = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        ctx.init(null, new TrustManager[]{trustAll}, new SecureRandom());

        SSLSocketFactory factory = ctx.getSocketFactory();
        socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        socket.setSoTimeout(0);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        pendingResponse.clear();

        readerThread = new Thread(this::readLoop, "hub-client-reader-" + clientID);
        readerThread.setDaemon(true);
        readerThread.start();

        heartbeatThread = new Thread(this::heartbeatLoop, "hub-client-heartbeat-" + clientID);
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    private void heartbeatLoop() {
        try {
            while (isConnected()) {
                Thread.sleep(1000);
                if (System.currentTimeMillis() - lastHeartbeat > 30*1000 && shouldBeSubscribed) {
                    sendHeartbeat();
                }
            }
        } catch (InterruptedException ignored) {

        }
    }

    private void sendHeartbeat() {
        logger.finest("Sending heartbeat...");

        synchronized (sendLock) {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put(TYPE_HEARTBEAT);

            try {
                sendFrame(buffer.array());
                byte res = awaitResponse()[0];
                if ((int) res != MessageBusBrokerResponses.ALL_GOOD.getCode()){

                    MessageBusBrokerResponses resE = MessageBusBrokerResponses.fromCode(res);
                    if (resE == null) {
                        logger.error("Heartbeat response is invalid! {}"+res);
                        return;
                    }
                    logger.warning("Heartbeat response is not ok {}", MessageBusBrokerResponses.fromCode(res));
                    if (resE == MessageBusBrokerResponses.NOT_AUTHORIZED) {
                        logger.error("Client sent an unauthorized heartbeat! Reconnecting...");
                        reconnect();
                    } else {
                        logger.error("Heartbeat response makes no sense! Reconnecting... {}" + res);
                        reconnect();
                    }

                } else {
                    logger.fine("Heartbeat ok!");
                    lastHeartbeat = System.currentTimeMillis();
                }
            } catch (IOException e) {
                logger.catching("Encountered an uncaught IOException while sending a heartbeat.", e);

            }

        }
    }

    private void readLoop() {
        try {
            while (isConnected()) {
                byte[] frame = readFrame();
                if (frame == null) continue;
                if (frame.length == 1) {
                    logger.finest("Received a response frame from the broker: {}", (Object) frame);
                    pendingResponse.offer(frame);
                } else if (frame.length > 0) {
                    logger.finest("Received a message frame from the broker: {}", (Object) frame);
                    handlePushedMessage(frame);
                }
            }
        } catch (EOFException e) {
            logger.warning("Connection closed by broker. Reconnecting...");
            reconnect();

        } catch (IOException e) {
            logger.catching(e);
            logger.error("Read loop ended. Reconnecting...");
            reconnect();
        }
    }

    private void handlePushedMessage(byte[] frame) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(frame);
            byte type = buf.get();
            if (type != TYPE_MESSAGE) return; // unknown/unsupported push type, ignore
            byte flags = buf.get();
            byte[] senderField = new byte[ID_LENGTH];
            buf.get(senderField);
            String sender = MessageBusUtil.fromFixedField(senderField);
            byte[] destField = new byte[ID_LENGTH];
            buf.get(destField);
            String destination = MessageBusUtil.fromFixedField(destField);
            byte[] jsonBytes = new byte[buf.remaining()];
            buf.get(jsonBytes);
            String json = new String(jsonBytes, StandardCharsets.UTF_8);

            Map<String, String> contents = JsonParser.parseString(json).getAsJsonObject().asMap()
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));

            boolean urgent = (flags & FLAG_URGENT) != 0;
            Message message = new Message(sender, destination, contents, urgent);
            logger.finest("Parsed message from the broker: {}", message);
            messageHandler.accept(message);
        } catch (Exception e) {
            logger.catching("Failed to handle received message", e);
        }
    }

    public void sendFrame(byte[] payload) throws IOException {
        try {
            synchronized (out) {
                out.writeInt(payload.length);
                out.write(payload);
                out.flush();
            }
        } catch (SocketException e) {
            logger.warning("Client could not connect to broker. Reconnecting...");
            reconnect();
        }
    }

    private final AtomicReference<Thread> reconnectThread = new AtomicReference<>();

    private synchronized void reconnect() {
        if (reconnectThread.get() != null) return;

        close(true);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);

                while (!Thread.currentThread().isInterrupted()) {
                    int res = subscribe();

                    if (res == MessageBusBrokerResponses.ALL_GOOD.getCode()) {
                        logger.info("Reconnected to broker!");
                        break;
                    }

                    Thread.sleep(10_000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                reconnectThread.set(null);
            }
        }, "messagebus-reconnect-" + clientID);

        reconnectThread.set(thread);
        thread.start();
    }


    public byte[] readFrame() throws IOException {
        int length = in.readInt();
        if (length < 0 || length > MAX_FRAME_SIZE) {
            throw new IOException("Invalid frame length from broker: " + length);
        }
        byte[] payload = new byte[length];
        in.readFully(payload);
        return payload;
    }

    private byte[] awaitResponse() throws IOException {
        try {
            byte[] res = pendingResponse.poll(10, TimeUnit.SECONDS);
            if (res == null) throw new SocketTimeoutException("No response from broker");
            return res;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while awaiting response", e);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void close(boolean isReconnect) {
        shouldBeSubscribed = false; // fixed: was `true`, backwards
        if (!isReconnect) logger.info("Closing client...");

        try {
            if (socket != null) socket.close();
            readerThread.interrupt();
            heartbeatThread.interrupt();
            if (reconnectThread.get() != null) reconnectThread.get().interrupt();
        } catch (IOException ignored) {
        }
    }

    private final ExecutorService senderExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "messagebus-sender-" + clientID);
                thread.setDaemon(true);
                return thread;
            });


    public CompletableFuture<Integer> sendMessageAsync(Message message) {
        return CompletableFuture.supplyAsync(
                () -> sendMessage(message),
                senderExecutor
        );

    }

    /**
     * WARNING! THIS METHOD IS BLOCKING!
     *
     * @param message
     * @return
     */
    public int sendMessage(Message message) {
        return sendMessage(message, 0);
    }

    /**
     * WARNING! THIS METHOD IS BLOCKING!
     *
     * @return
     */
    public int subscribe() {
        synchronized (sendLock) {
            logger.info("Subscribing to broker as {}...", clientID);
            logger.finest("Subscribe method called");

            if (shouldBeSubscribed) return MessageBusBrokerResponses.ALL_GOOD.getCode();
            if (!isConnected()) {
                try {
                    try {
                        connect();
                    } catch (SocketException e) {
                        logger.warning("An error occurred while trying to subscribe! Reconnecting...");
                        reconnect();
                        return -1;
                    }
                    shouldBeSubscribed = false;
                } catch (Exception e) {
                    logger.catching(e);
                    return -1;
                }
            }
            logger.finest("All checks passed..");
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1 + ID_LENGTH + PASS_LENGTH);
                buffer.put(TYPE_SUBSCRIBE);
                buffer.put(MessageBusUtil.toFixedField(clientID, ID_LENGTH));
                buffer.put(MessageBusUtil.toFixedField(pass, PASS_LENGTH));
                logger.finest("Sending Frame..");
                sendFrame(buffer.array());
                logger.finest("Frame sent, awaiting response...");
                byte[] res = awaitResponse();
                int resCode = Byte.toUnsignedInt(res[0]);
                logger.finest("Response code: " + resCode);
                if (resCode != MessageBusBrokerResponses.ALL_GOOD.getCode()) {
                    if (resCode == MessageBusBrokerResponses.BAD_PASSWORD.getCode()) {
                        logger.critical("Client owns an invalid password. Please check the configuration");

                        close(false);
                        return resCode;
                    } else if (resCode == MessageBusBrokerResponses.ALREADY_AUTHORIZED.getCode()) {
                        logger.warning("Broker says the client is already authorized. Moving on...");
                        shouldBeSubscribed = true;
                        return MessageBusBrokerResponses.ALL_GOOD.getCode();
                    } else if (resCode == MessageBusBrokerResponses.CLIENT_ID_ALREADY_IN_USE.getCode()) {
                        logger.critical("Broker says the client's id is already being used (Please check the config). Reconnecting...");
                        reconnect();
                        return resCode;
                    } else {
                        logger.warning("Got error code " + MessageBusBrokerResponses.fromCode(resCode) + " which makes next to no sense in auth.");
                        return resCode;
                    }
                } else {
                    logger.info("Successfully subscribed to broker!");
                    shouldBeSubscribed = true;
                    return resCode;
                }

            } catch (SocketTimeoutException e) {
                logger.warning("Broker did not respond in time, reconnecting...");
                reconnect();
                return -1;
            } catch (Exception e) {
                logger.catching(e);
                return -1;
            }
        }
    }

    /**
     * WARNING! THIS METHOD IS BLOCKING!
     *
     * @param message
     * @param retryNum
     * @return
     */
    public int sendMessage(Message message, int retryNum) {
        if (retryNum > 0) {
            logger.error("Was unable to properly communicate with the broker after 3 times!");
            return -1;
        }

        synchronized (sendLock) { // serialize: only one in-flight request/response at a time
            if (!isConnected()) {
                try {
                    connect();
                    shouldBeSubscribed = false;
                } catch (Exception e) {
                    logger.catching(e);
                    return -1;
                }
            }

            try {
                if (!shouldBeSubscribed) {
                    int res = subscribe();
                    if (res != MessageBusBrokerResponses.ALL_GOOD.getCode()) {
                        return res;
                    }
                }

                String json = gson.toJson(message.contents());
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

                ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + ID_LENGTH + jsonBytes.length);
                buffer.put(TYPE_MESSAGE);
                byte flags = 0x00;
                if (message.urgent()) flags |= FLAG_URGENT;
                buffer.put(flags);
                buffer.put(MessageBusUtil.toFixedField(message.destination(), ID_LENGTH));
                buffer.put(jsonBytes);

                sendFrame(buffer.array());
                byte[] res = awaitResponse();
                int resCode = Byte.toUnsignedInt(res[0]);

                if (resCode != MessageBusBrokerResponses.ALL_GOOD.getCode()) {
                    if (resCode == MessageBusBrokerResponses.NOT_AUTHORIZED.getCode()) {
                        logger.warning("Broker says the client is not authorized. Retrying...");
                        shouldBeSubscribed = false;
                        return sendMessage(message, retryNum + 1);
                    } else if (resCode == MessageBusBrokerResponses.FRAME_TOO_LARGE.getCode()) {
                        logger.error("Attempted to send a frame larger than allowed!");
                        return resCode;
                    } else if (resCode == MessageBusBrokerResponses.MALFORMED_JSON.getCode()) {
                        logger.error("Malformed JSON sent to the broker! Retrying...");
                        return sendMessage(message, retryNum + 1);
                    } else {
                        logger.warning("Got error code " + MessageBusBrokerResponses.fromCode(resCode) + " which makes next to no sense here.");
                        return resCode;
                    }
                }
                return resCode;
            } catch (SocketTimeoutException e) {
                logger.warning("Broker did not respond in time, reconnecting...");
                reconnect();
                return -1;
            } catch (SocketException e) {
                logger.warning("An error occurred while trying to send the message! Reconnecting...");
                reconnect();
                return -1;
            } catch (Exception e) {
                logger.catching(e);
                return -1;
            }
        }
    }


}