package dev.redgamer6427a.core.minecraft.paper.player.modules;

import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.minecraft.paper.player.ExtendedPlayer;
import dev.redgamer6427a.core.minecraft.paper.player.PlayerModule;
import dev.redgamer6427a.core.minecraft.paper.util.constants.EntityEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class PacketModule extends PlayerModule {


    public PacketModule(ExtendedPlayer player) {
        super(player);
    }

    public void sendPacket(Packet<?> packet) {
        getServerPlayer().connection.send(packet);
    }

    public void sendEntityStatus(Entity entity, EntityEvent event) {

        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        sendPacket(new ClientboundEntityEventPacket(nmsEntity, event.byteCode()));

    }

    public void sendEntityStatus(EntityEvent event) {
        sendEntityStatus(player.getPlayer(), event);
    }

    private static final Logger logger = Logger.create();


    public void sendOpStatus(int op){

        if (op > 4 || op < 0){
            throw logger.throwing(new IllegalArgumentException("Op level must be between 0 and 4. Got "+op));
        }

        EntityEvent e = switch (op) {
            case 0 -> EntityEvent.OP_LEVEL_0;
            case 1 -> EntityEvent.OP_LEVEL_1;
            case 2 -> EntityEvent.OP_LEVEL_2;
            case 3 -> EntityEvent.OP_LEVEL_3;
            case 4 -> EntityEvent.OP_LEVEL_4;

            default -> throw new IllegalStateException("Unexpected value: " + op);
        };

        sendEntityStatus(e);

    }

}
