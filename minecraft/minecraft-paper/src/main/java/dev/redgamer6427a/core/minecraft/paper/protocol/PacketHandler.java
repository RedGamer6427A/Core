package dev.redgamer6427a.core.minecraft.paper.protocol;

import net.minecraft.network.protocol.Packet;

import java.io.IOException;

@FunctionalInterface
public interface PacketHandler<T extends Packet<?>> {

    void handle(T packet, PacketContext<T> player) throws IOException;


}
