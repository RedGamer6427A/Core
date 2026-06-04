package dev.redgamer6427a.core.minecraft.paper.item;

import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import static org.bukkit.DyeColor.*;

public class ItemUtils {

    public static boolean hasTag(@Nullable ItemStack item, String tag) {

        return item != null
                &&
                Boolean.TRUE.equals(
                        item.getPersistentDataContainer()
                                .get(
                                        new NamespacedKey(PaperPlugin.getInstance(), tag
                                ),
                                PersistentDataType.BOOLEAN
                        )
                );
    }

    public static ItemStack ominousBannerPattern(ItemStack item) {

        return new ExtendedItemStack(item)
                .resetBannerPatterns()
                .addBannerPattern(new Pattern(CYAN, PatternType.RHOMBUS))
                .addBannerPattern(new Pattern(LIGHT_GRAY, PatternType.STRIPE_BOTTOM))
                .addBannerPattern(new Pattern(GRAY, PatternType.STRIPE_CENTER))
                .addBannerPattern(new Pattern(LIGHT_GRAY, PatternType.BORDER))
                .addBannerPattern(new Pattern(BLACK, PatternType.STRIPE_MIDDLE))
                .addBannerPattern(new Pattern(LIGHT_GRAY, PatternType.HALF_HORIZONTAL))
                .addBannerPattern(new Pattern(LIGHT_GRAY, PatternType.CIRCLE))
                .addBannerPattern(new Pattern(BLACK, PatternType.BORDER))
                .get();

    }

    public static ItemStack ominousBanner(){
        return new ExtendedItemStack(ominousBannerPattern(new ItemStack(Material.WHITE_BANNER))).hideComponents(DataComponentTypes.BANNER_PATTERNS).get();
    }

    public static Integer getCustomIntData(String key, ItemStack stack) {
        if (stack == null) return null;
        NamespacedKey nKey = new NamespacedKey(PaperPlugin.getInstance(), key);
        return stack.getPersistentDataContainer().get(nKey, PersistentDataType.INTEGER);
    }

    public static Double getCustomDoubleData(String key, ItemStack stack) {
        if (stack == null) return null;
        NamespacedKey nKey = new NamespacedKey(PaperPlugin.getInstance(), key);
        return stack.getPersistentDataContainer().get(nKey, PersistentDataType.DOUBLE);
    }

    public static String getCustomStringData(String key, ItemStack stack) {
        if (stack == null) return null;
        NamespacedKey nKey = new NamespacedKey(PaperPlugin.getInstance(), key);
        return stack.getPersistentDataContainer().get(nKey, PersistentDataType.STRING);
    }

    public static Boolean getCustomBooleanData(String key, ItemStack stack) {
        if (stack == null) return null;
        NamespacedKey nKey = new NamespacedKey(PaperPlugin.getInstance(), key);
        return stack.getPersistentDataContainer().get(nKey, PersistentDataType.BOOLEAN);
    }

    public static Location getCustomLocationData(String key, ItemStack stack) {
        if (stack == null) return null;
        try {
            return new Location(
                    Bukkit.getWorld(stack.getPersistentDataContainer().get(new NamespacedKey(PaperPlugin.getInstance(), key+".world"), PersistentDataType.STRING)),
                    stack.getPersistentDataContainer().get(new NamespacedKey(PaperPlugin.getInstance(), key+".x"), PersistentDataType.DOUBLE),
                    stack.getPersistentDataContainer().get(new NamespacedKey(PaperPlugin.getInstance(), key+".y"), PersistentDataType.DOUBLE),
                    stack.getPersistentDataContainer().get(new NamespacedKey(PaperPlugin.getInstance(), key+".z"), PersistentDataType.DOUBLE),
                    stack.getPersistentDataContainer().get(new NamespacedKey(PaperPlugin.getInstance(), key+".yaw"), PersistentDataType.FLOAT),
                    stack.getPersistentDataContainer().get(new NamespacedKey(PaperPlugin.getInstance(), key+".pitch"), PersistentDataType.FLOAT)

            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
