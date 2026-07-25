package dev.redgamer6427a.core.minecraft.paper.testing.messaging;

import dev.redgamer6427a.core.messagebus.packet.Packet;

public record GlobalMSGPacket(String message) implements Packet {
    @Override
    public String getPacketType() {
        return "global-msg-packet";
    }

    @Override
    public String defaultDestination() {
        return "*";
    }

    @Override
    public Boolean defaultUrgent() {
        return false;
    }
}
