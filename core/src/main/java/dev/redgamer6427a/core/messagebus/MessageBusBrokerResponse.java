package dev.redgamer6427a.core.messagebus;

import lombok.Getter;

public enum MessageBusBrokerResponse {
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

    MessageBusBrokerResponse(int code) {
        this.code = code;
    }

    public static MessageBusBrokerResponse fromCode(int code) {
        for (MessageBusBrokerResponse value : MessageBusBrokerResponse.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

}
