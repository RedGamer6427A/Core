package dev.redgamer6427a.core.messagebus.server;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.messagebus.MessageBusBrokerResponses;
import dev.redgamer6427a.core.messagebus.MessageBusUtil;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.redgamer6427a.core.messagebus.MessageBusConstants.*;

/**
 * PROTOCOL
 * <p>
 * Subscribe: client says hi I exist please give me the tea
 * (name, password, )
 * Message: puts a message on top of the message list to be processed (or ignored) by the destinations
 * <p>
 * ERRORS:
 * 0 - all good
 * 1 - bad pass
 * 2 - bad message json
 * 3 - id already used
 * 4 - not authorized
 * 5 - ratelimited
 */

// TODO: put error numbers into constants
public class ClientConnection implements Runnable {


    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final BrokerThread brokerThread;
    private final String pass;
    private static final Logger logger = Logger.create();

    // Client Data
    @Getter
    private boolean authorized = false;
    @Getter
    private String clientId = null;

    public ClientConnection(Socket socket, BrokerThread brokerThread, String pass) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(30_000);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        this.brokerThread = brokerThread;
        this.pass = pass;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {

                byte[] frame = readFrame(in);      // read incoming
                if (frame == null) continue;
                handle(frame);                      // process
            }
            logger.info("Connection closed: {} (ip: {}, authorized: {})", clientId, socket.getInetAddress().getHostAddress(), authorized);

        } catch (SocketTimeoutException e) {
            logger.warning("Client {} timed out before authenticating", socket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            logger.catching("Client connection closed", e);
        } catch (RuntimeException e) {
            logger.catching("Unexpected error in client connection", e);
        } finally {
            brokerThread.unregisterClient(this);
        }
    }



    private byte[] readFrame(DataInputStream in) throws IOException {
        int length = in.readInt();           // blocks til 4 bytes available
        if (length < 0) {
            throw new IOException("Invalid frame length: " + length);
        }
        if (length > MAX_FRAME_SIZE) {
            error(MessageBusBrokerResponses.FRAME_TOO_LARGE.getCode()); // new code: frame too large
            out.flush();
            // still need to drain the oversized payload off the stream, or next read desyncs
            in.skipNBytes(length);
            return null; // signal caller to skip processing this frame
        }

        byte[] payload = new byte[length];
        in.readFully(payload);               // blocks til exactly `length` bytes read
        return payload;
    }


    private boolean passMatches(byte[] sentPass) {
        byte[] expected = new byte[64];
        byte[] passBytes = pass.getBytes(StandardCharsets.US_ASCII);

        if (passBytes.length > expected.length) {
            return false;
        }

        System.arraycopy(passBytes, 0, expected, 0, passBytes.length);

        return MessageDigest.isEqual(expected, sentPass);
    }

    private void handle(byte[] frame) throws IOException {

        ByteBuffer buf = ByteBuffer.wrap(frame); // wraps existing array, no copy
        byte type = buf.get();

        switch (type) {
            case TYPE_SUBSCRIBE -> {
                if (authorized) {
                    error(MessageBusBrokerResponses.ALREADY_AUTHORIZED.getCode());
                    return;
                }
                byte[] idField = new byte[ID_LENGTH];
                buf.get(idField);
                String clientId = MessageBusUtil.fromFixedField(idField);

                byte[] passField = new byte[PASS_LENGTH];
                buf.get(passField);

                if (passMatches(passField)) {

                    this.clientId = clientId;
                    if (brokerThread.registerClient(this) != null) {
                        error(MessageBusBrokerResponses.CLIENT_ID_ALREADY_IN_USE.getCode());
                        out.flush();
                        socket.close();
                        return;
                    }

                    authorized = true;
                    socket.setSoTimeout(0);
                } else {
                    this.clientId = clientId;
                    try {
                        brokerThread.recordAuthFailure(socket.getInetAddress().getHostAddress());
                        error(MessageBusBrokerResponses.BAD_PASSWORD.getCode());
                        out.flush();
                    } finally {
                        socket.close();
                    }

                    return;
                }

            }
            case TYPE_MESSAGE -> {
                if (!authorized) {
                    error(MessageBusBrokerResponses.NOT_AUTHORIZED.getCode());
                    return;
                }

                byte flags = buf.get();

                byte[] destField = new byte[ID_LENGTH];
                buf.get(destField);
                String destination = MessageBusUtil.fromFixedField(destField);

                byte[] jsonBytes = new byte[buf.remaining()];
                buf.get(jsonBytes);
                String json = new String(jsonBytes, StandardCharsets.UTF_8);
                Map<String, String> contents;
                try {
                    contents = JsonParser.parseString(json).getAsJsonObject().asMap().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));
                } catch (Exception e) {
                    error(MessageBusBrokerResponses.MALFORMED_JSON.getCode());
                    return;
                }
                boolean isUrgent = (flags & FLAG_URGENT)   != 0; // Coming through!!!!
                Message message = new Message(destination, clientId, contents, isUrgent);
                if (isUrgent) {
                    brokerThread.getMessages().addFirst(message);
                } else {
                    brokerThread.getMessages().add(message);
                }

            }
        }

        ackFrame();
    }

    private void ackFrame() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.put((byte) MessageBusBrokerResponses.ALL_GOOD.getCode());
        sendResponse(buf.array());
    }

    private void error(int code) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.put((byte) code);

        logger.warning("Client connection error: {} (ip: {}, authorized: {}, id: '{}')", MessageBusBrokerResponses.fromCode(code), socket.getInetAddress().getHostAddress(), authorized, clientId);

        sendResponse(buf.array());

    }

    Gson gson = new Gson();

    public void pushMessage(Message msg) throws IOException {
        String json = gson.toJson(msg.contents());
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        byte flags = (byte) (msg.urgent() ? FLAG_URGENT : 0);

        ByteBuffer buf = ByteBuffer.allocate(1 + 1 + ID_LENGTH + ID_LENGTH + jsonBytes.length);
        buf.put(TYPE_MESSAGE);
        buf.put(flags);
        buf.put(MessageBusUtil.toFixedField(msg.sender(), ID_LENGTH));
        buf.put(MessageBusUtil.toFixedField(msg.destination(), ID_LENGTH));
        buf.put(jsonBytes);

        sendResponse(buf.array()); // reuses existing synchronized(out) writer
    }

    private void sendResponse(byte[] data) throws IOException {
        synchronized (out) {                         // guard if multiple threads write
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        }
    }
}