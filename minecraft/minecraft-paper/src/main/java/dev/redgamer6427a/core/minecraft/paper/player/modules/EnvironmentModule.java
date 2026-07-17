package dev.redgamer6427a.core.minecraft.paper.player.modules;

import dev.redgamer6427a.core.minecraft.paper.player.ExtendedPlayer;
import dev.redgamer6427a.core.minecraft.paper.player.PlayerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;

import java.util.function.Function;

public final class EnvironmentModule extends PlayerModule {


    private EnvironmentModule(ExtendedPlayer player) {
        super(player);

    }

    public void sendBlock(Location location, Material block, Function<Block, BlockState> blockStateAction) {
        ServerPlayer serverPlayer = ((CraftPlayer) player.getPlayer()).getHandle();
        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(
                new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                blockStateAction.apply(CraftBlockType.bukkitToMinecraft(block))
        );
        serverPlayer.connection.send(packet);
    }

    public void sendSwingHand(Entity entity, boolean isMainHand) {
        sendPacket(
                new ClientboundAnimatePacket(((CraftEntity) entity).getHandle(), isMainHand ? ClientboundAnimatePacket.SWING_MAIN_HAND : ClientboundAnimatePacket.SWING_OFF_HAND)
        );
    }

    public void sendTickState(float tickRate, boolean freeze) {
        sendPacket(
                new ClientboundTickingStatePacket(tickRate, freeze)
        );
    }

    public void sendTickStep(int tickSteps) {
        sendPacket(
                new ClientboundTickingStepPacket(tickSteps)
        );
    }

}
