package dev.redgamer6427a.core.minecraft.velocity.testing;

import dev.redgamer6427a.core.messagebus.packet.Packet;

public record CommandBroadcastPacket(String command) implements Packet {
    @Override
    public String getPacketType() {
        return "command_broadcast";
    }

    @Override
    public String defaultDestination() {
        return "*";
    }

    @Override
    public Boolean defaultUrgent() {
        return true;
    }
}
