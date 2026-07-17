package dev.redgamer6427a.core.minecraft.paper.player.modules;

import dev.redgamer6427a.core.minecraft.paper.player.ExtendedPlayer;
import dev.redgamer6427a.core.minecraft.paper.player.PlayerModule;
import dev.redgamer6427a.core.minecraft.paper.util.Procrastinator;
import dev.redgamer6427a.core.minecraft.paper.util.constants.EntityEvent;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class HUDModule extends PlayerModule {


    private static final Map<Player, Long> displayMap = new HashMap<>();

    public HUDModule(ExtendedPlayer player) {
        super(player);
    }

    public void displayRedVignette() {
        WorldBorder worldBorder = player.getPlayer().getWorld().getWorldBorder();
        WorldBorder fakeBorder = Bukkit.createWorldBorder();
        fakeBorder.setSize(worldBorder.getSize());
        fakeBorder.setCenter(worldBorder.getCenter());
        fakeBorder.setDamageAmount(worldBorder.getDamageAmount());
        fakeBorder.setWarningDistance(1_000_000_000);
        player.getPlayer().setWorldBorder(fakeBorder);

        displayMap.put(player.getPlayer(), -1L);
    }

    public void displayBorder(int ticks) {
        displayRedVignette();

        long expireAt = Bukkit.getCurrentTick() + ticks;
        displayMap.put(player.getPlayer(), expireAt);

        Procrastinator.later(ticks, () -> {
            Long deadline = displayMap.get(player.getPlayer());
            if (deadline != null && deadline <= Bukkit.getCurrentTick()) {
                hideRedVignette();
            }
        });

    }

    public void sendWakeUpAnimation() {
        sendPacket(
                new ClientboundAnimatePacket(player.getServerPlayer(), ClientboundAnimatePacket.WAKE_UP)
        );

    }

    public void sendTotemPop(Entity entity) {

        net.minecraft.world.entity.Entity serverEntity = ((CraftEntity) entity).getHandle();

        sendPacket(new ClientboundEntityEventPacket(serverEntity, EntityEvent.TOTEM_OF_UNDYING.byteCode()));
    }

    public void sendTotemPop() {
        sendTotemPop(player.getPlayer());
    }

    public void sendReducedDebugInformation(boolean reduced){
        byte eventID = reduced ? EntityEvent.PLAYER_DEBUG_SCREEN_REDUCED_ON.byteCode() : EntityEvent.PLAYER_DEBUG_SCREEN_REDUCED_OFF.byteCode();

        sendPacket(new ClientboundEntityEventPacket(getServerPlayer(), eventID));
    }

    public void hideRedVignette() {
        player.getPlayer().setWorldBorder(null);
        displayMap.remove(player.getPlayer());
    }
}
