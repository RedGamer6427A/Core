package dev.redgamer6427a.core.messagebus;

import java.util.Map;


public record Message(String destination, String sender, Map<String, String> contents, boolean urgent) {

    public Message(String destination, Map<String, String> contents, boolean urgent) {
        this(destination, "this", contents, urgent);
    }

}
