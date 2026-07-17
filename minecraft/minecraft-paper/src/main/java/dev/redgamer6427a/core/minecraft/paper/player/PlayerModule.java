package dev.redgamer6427a.core.minecraft.paper.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public abstract class PlayerModule {

    protected final ExtendedPlayer player;

    public PlayerModule(ExtendedPlayer player) {
        this.player = player;
    }

    protected ServerPlayer getServerPlayer() {
        return ((CraftPlayer) player.getPlayer()).getHandle();
    }

    protected void sendPacket(Packet<?> packet){
        getServerPlayer().connection.send(packet);
    }

}
