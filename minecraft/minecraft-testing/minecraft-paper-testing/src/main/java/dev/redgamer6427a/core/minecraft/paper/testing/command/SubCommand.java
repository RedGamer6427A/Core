package dev.redgamer6427a.core.minecraft.paper.testing.command;

import ca.spottedleaf.concurrentutil.completable.Completable;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.minecraft.paper.command.AllowedSources;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.command.argument.Argument;
import dev.redgamer6427a.core.minecraft.paper.testing.PaperTestPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SubCommand extends BrigadierCommand {


    public SubCommand() {
        super("sub", "i should lk remove this");

        setDefaultExecutor(context -> {

            if (context.getSource().getSender() instanceof Player player) {
                ServerPlayer sp = ((CraftPlayer) player).getHandle();

                ClientboundEntityEventPacket packet = new ClientboundEntityEventPacket(sp, (byte) 35);

                sp.connection.send(packet);
            }
        }, AllowedSources.PLAYER);
    }

}
