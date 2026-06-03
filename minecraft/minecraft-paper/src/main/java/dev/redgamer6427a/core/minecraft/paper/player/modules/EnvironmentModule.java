package dev.redgamer6427a.admiral.paper.player.modules;

import dev.redgamer6427a.admiral.paper.player.ExtendedPlayer;
import dev.redgamer6427a.admiral.paper.player.PlayerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.entity.CraftPlayer;

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
    }
