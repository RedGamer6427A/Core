package dev.redgamer6427a.core.minecraft.velocity.testing;

import dev.redgamer6427a.core.messagebus.packet.Packet;

public record ProxyExecuteCommandPacket(String command) implements Packet {
    @Override
    public String getPacketType() {
        return "proxy_execute_command";
    }

    @Override
    public String defaultDestination() {
        return "broker";
    }

    @Override
    public Boolean defaultUrgent() {
        return true;
    }
}
