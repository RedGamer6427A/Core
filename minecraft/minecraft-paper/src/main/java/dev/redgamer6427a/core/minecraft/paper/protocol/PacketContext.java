package dev.redgamer6427a.core.minecraft.paper.protocol;

import dev.redgamer6427a.core.minecraft.paper.player.ExtendedPlayer;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;


@Getter
public class PacketContext<T extends Packet<?>> {

    private final ExtendedPlayer player;
    private final T original;
    private final PacketFlow flow;
    private final ChannelHandlerContext channelHandlerContext;

    @Setter
    private boolean cancelled;
    @Setter
    private T result;

    public PacketContext(ExtendedPlayer player, T packet, PacketFlow flow, ChannelHandlerContext channelHandlerContext) {
        this.player = player;
        this.original = packet;
        this.result = packet;
        this.flow = flow;
        this.channelHandlerContext = channelHandlerContext;
        this.cancelled = false;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
