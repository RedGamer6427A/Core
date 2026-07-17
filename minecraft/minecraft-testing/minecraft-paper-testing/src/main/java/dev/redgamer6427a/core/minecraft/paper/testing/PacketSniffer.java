package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.logging.Logger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PacketSniffer implements Listener {
    private static final Logger logger = Logger.create();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        hook(player);
    }

    public void hook(Player bukkitPlayer) {
        logger.info("hooking");
        ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        ServerGamePacketListenerImpl connection = nmsPlayer.connection;
        Channel channel = connection.connection.channel; // Connection.channel field

        channel.pipeline().addBefore("packet_handler", "silly-sniffer", new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                boolean cancelled = false;
                if (msg instanceof Packet<?> packet && packet instanceof ServerboundChatPacket specPacket) {

                    bukkitPlayer.sendMessage(specPacket.message());
                    cancelled = true;
                }

                if (!cancelled) super.channelRead(ctx, msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                bukkitPlayer.getServer().getLogger().info("OUT: " + msg.getClass().getSimpleName());
                super.write(ctx, msg, promise);
            }
        });
    }
}