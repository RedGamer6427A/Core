package dev.redgamer6427a.core.minecraft.paper.testing.command;

import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.command.AllowedSources;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.mm;

public class SubCommand extends BrigadierCommand {


    public SubCommand() {
        super("sub", "i should lk remove this");

        setDefaultExecutor(context -> {

            if (context.getSource().getSender() instanceof Player player) {

                String minimessage = "<head_texture_hash:958abf349395285ae0e6647044a36cb4357b98c5d52bb05022785f10ecff1a3f><green>HAI<head_texture_hash:e4ab55bb786d81a64f368282c33637cf6ccb0c4271e1dfc0dafc8b281c1fbbca><head_texture:2012><head_texture:evil>";
                player.sendMessage(mm("<green>Minimessage: <gray>").append(Component.text(minimessage)));
                Component component = mm(minimessage);
                answer(context, mm("<blue>Component: ").append(component));
                player.sendMessage(mm("<pink>Parsed Minimessage: <gray>").append(Component.text(AdventureMM.serialize(component))));
                answer(context, "<purple>Parsed Component: <gray>" + AdventureMM.serialize(component));



            }
        }, AllowedSources.PLAYER);
    }

}
