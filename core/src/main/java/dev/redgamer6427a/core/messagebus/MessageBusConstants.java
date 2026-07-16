package dev.redgamer6427a.core.messagebus;

public class MessageBusConstants {
    public static final byte TYPE_HEARTBEAT = 0;
    public static final byte TYPE_SUBSCRIBE = 1;
    public static final byte TYPE_MESSAGE   = 2;
    public static final byte FLAG_URGENT    = 0x01;
    public static final int  ID_LENGTH      = 64;
    public static final int  PASS_LENGTH      = 64;
    public static final int  MAX_FRAME_SIZE = 1024 * 1024;



}
