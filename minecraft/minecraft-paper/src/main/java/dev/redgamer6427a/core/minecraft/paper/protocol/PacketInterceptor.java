package dev.redgamer6427a.core.minecraft.paper.protocol;

import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.player.ExtendedPlayer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class PacketInterceptor implements Listener {

    private static final String BASE_NAME = "packet_handler";
    private static String NAME;

    private static final int MAX_RETRIES = 20; // 40 ticks = 2 sec at 20tps
    private static final long RETRY_DELAY_TICKS = 1L;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleJoin(PlayerJoinEvent event) {
        if (enabled) {
            attemptHook(event.getPlayer(), 0);
        }
    }

    private static void attemptHook(Player bukkitPlayer, int attempt) {
        if (!bukkitPlayer.isOnline()) return;

        Channel channel = ExtendedPlayer.of(bukkitPlayer).getServerPlayer().connection.connection.channel;

        Bukkit.getLogger().info("attempt " + attempt + " pipeline: " + channel.pipeline().names());

        if (channel.pipeline().get(BASE_NAME) != null) {
            try {
                hook(bukkitPlayer);
                return;
            } catch (Exception e) {
                logger.catching(e);
            }

        }

        if (attempt >= MAX_RETRIES) {
            Bukkit.getLogger().warning("gave up after " + MAX_RETRIES);
            return;
        }

        Bukkit.getScheduler().runTaskLater(PaperPlugin.getInstance(), () -> attemptHook(bukkitPlayer, attempt + 1), RETRY_DELAY_TICKS);
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent event) {
        unhook(event.getPlayer()); // Do I even need this?
    }

    private static boolean enabled = false;

    private static final List<Map.Entry<Class<? extends Packet<?>>, PacketHandler<? extends Packet<?>>>> handlers = new ArrayList<>();


    public static <T extends Packet<?>> void addHandler(Class<T> packetClass, PacketHandler<T> packetHandler) {
        handlers.add(Map.entry(packetClass, packetHandler));

    }

    public static void enable() {
        if (NAME == null) {
            NAME = PaperPlugin.getInstance().getPluginMeta().getName() + ":packet_interceptor";
        }
        enabled = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            hook(player);
        }
    }

    public static void disable() {
        enabled = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            unhook(player);
        }
    }



    private static Object handle(ChannelHandlerContext ctx, ExtendedPlayer player, Packet<?> packet) {
        Packet<?> result = packet;

        for (Map.Entry<Class<? extends Packet<?>>, PacketHandler<? extends Packet<?>>> entry : handlers) {
            if (result == null) break; // already cancelled, stop dispatching further

            Class<? extends Packet<?>> packetClass = entry.getKey();
            if (packetClass.isAssignableFrom(result.getClass())) {
                result = dispatch(packetClass, entry.getValue(), player, result, ctx);
            }
        }
        return result;
    }

    private static <T extends Packet<?>> T dispatch(
            Class<T> packetClass,
            PacketHandler<? extends Packet<?>> rawHandler,
            ExtendedPlayer player,
            Packet<?> packet,
            ChannelHandlerContext ctx
    ) {
        @SuppressWarnings("unchecked")
        PacketHandler<T> handler = (PacketHandler<T>) rawHandler;

        T casted = packetClass.cast(packet);
        PacketContext<T> context = new PacketContext<>(player, casted, packet.type().flow(), ctx);

        try {
            handler.handle(casted, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (context.isCancelled()) {
            return null;
        }
        return context.getResult();
    }
    private static final Logger logger = Logger.create();


    public static void hook(Player bukkitPlayer) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.of(bukkitPlayer);
        Channel channel = extendedPlayer.getServerPlayer().connection.connection.channel;
        Object conn = extendedPlayer.getServerPlayer().connection;
        Bukkit.getLogger().info("connection class: " + conn.getClass().getName());
        Bukkit.getLogger().info("channel class: " + channel.getClass().getName());
        Bukkit.getLogger().info("channel active: " + channel.isActive());
        Bukkit.getLogger().info("channel open: " + channel.isOpen());
        channel.pipeline().addBefore(BASE_NAME, NAME, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                if (msg instanceof Packet<?> packet) {
                    Object result = handle(ctx, extendedPlayer, packet);
                    if (result != null) {
                        super.channelRead(ctx, result);
                    }
                } else {
                    super.channelRead(ctx, msg);
                }
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof Packet<?> packet) {
                    Object result = handle(ctx, extendedPlayer, packet);
                    if (result != null) {
                        super.write(ctx, result, promise);
                    } else {
                        promise.setSuccess();
                    }
                } else {
                    super.write(ctx, msg, promise);
                }
            }
        });
        logger.fine("Successfully hooked into {}'s connection!", bukkitPlayer.getName());

    }

    public static void unhook(Player bukkitPlayer) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.of(bukkitPlayer);
        Channel channel = extendedPlayer.getServerPlayer().connection.connection.channel;

        try {
            channel.pipeline().remove(NAME);
        } catch (NoSuchElementException ignored) {}
    }

}
