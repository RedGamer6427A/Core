package dev.redgamer6427a.core.minecraft.paper.menu.pagedMenu;


import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.item.ExtendedItemStack;
import dev.redgamer6427a.core.minecraft.paper.menu.ClickableItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.cc;


public class PagedMenuItems {

    public static <M extends PagedMenu<?>> ClickableItemStack forwardItem(
            M menu, BiFunction<Player, Integer, M> factory) {

        return ClickableItemStack.item(
                new ExtendedItemStack(Material.PLAYER_HEAD)
                        .skullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJiMGMwN2ZhMGU4OTIzN2Q2NzllMTMxMTZiNWFhNzVhZWJiMzRlOWM5NjhjNmJhZGIyNTFlMTI3YmRkNWIxIn19fQ==")
                        .itemName(AdventureMM.cc("<green>Next"))
                        .get(),
                event -> {
                    Player player = (Player) event.getWhoClicked();
                    factory.apply(player, menu.page + 1);

                },
                ClickableItemStack.DEFAULT_SOUND_TRAVEL()
        );
    }

    public static <M extends PagedMenu<?>> ClickableItemStack backwardItem(
            M menu, BiFunction<Player, Integer, M> factory) {

        return ClickableItemStack.item(
                new ExtendedItemStack(Material.PLAYER_HEAD)
                        .skullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU5YmUxNTU3MjAxYzdmZjFhMGIzNjk2ZDE5ZWFiNDEwNDg4MGQ2YTljZGI0ZDVmYTIxYjZkYWE5ZGIyZDEifX19")
                        .itemName(AdventureMM.cc("<green>Back"))
                        .get(),
                event -> {
                    Player player = (Player) event.getWhoClicked();
                    factory.apply(player, menu.page - 1);

                },
                ClickableItemStack.DEFAULT_SOUND_TRAVEL()
        );
    }


}
