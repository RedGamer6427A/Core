package dev.redgamer6427a.core.messagebus;

import lombok.Getter;

public enum MessageBusBrokerResponses {
    ALL_GOOD(0),
    BAD_PASSWORD(1),
    MALFORMED_JSON(2),
    CLIENT_ID_ALREADY_IN_USE(3),
    NOT_AUTHORIZED(4),
    FRAME_TOO_LARGE(5),
    ALREADY_AUTHORIZED(6),

    ;
    @Getter
    private final int code;

    MessageBusBrokerResponses(int code) {
        this.code = code;
    }

    public static MessageBusBrokerResponses fromCode(int code) {
        for (MessageBusBrokerResponses value : MessageBusBrokerResponses.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

}
