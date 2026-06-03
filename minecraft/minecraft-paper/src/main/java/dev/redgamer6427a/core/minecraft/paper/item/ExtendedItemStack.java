package dev.redgamer6427a.admiral.paper.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtendedItemStack {

    private final ItemStack item;

    public ExtendedItemStack(ItemStack item1) {
        item = item1;

    }

    public ExtendedItemStack(Material material) {
        item = new ItemStack(material);

    }
    public ExtendedItemStack setCount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ExtendedItemStack itemName(Component name) {
        ItemMeta meta = item.getItemMeta();
        meta.itemName(name);
        item.setItemMeta(meta);
        return this;
    }


    public ExtendedItemStack setCustomName(Component name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return this;
    }

    public ExtendedItemStack addLore(Component lore) {
        ItemMeta meta = item.getItemMeta();
        lore = lore.applyFallbackStyle(Style.style(TextDecoration.ITALIC.withState(false)));
        if (meta.lore() == null) {
            meta.lore(List.of(lore));
        } else {
            List<Component> newLore = meta.lore();
            newLore.add(lore);
            meta.lore(newLore );
        }
        item.setItemMeta(meta);
        return this;
    }
    public ExtendedItemStack addLore(List<Component> lore){
        if(lore.isEmpty()){
            return this;
        }
        for(Component component : lore){
            addLore(component);

        }
        return this;
    }



    public ExtendedItemStack addCustomModelData(String data) {
        ItemMeta meta = item.getItemMeta();
        List<String> stringList = new ArrayList<>(meta.getCustomModelDataComponent().getStrings());
        stringList.add(data);
        meta.getCustomModelDataComponent().setStrings(stringList);
        item.setItemMeta(meta);
        return this;
    }

    public ExtendedItemStack hideFlags(ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();

        // Add the hide flag for attributes
        meta.addItemFlags(flags);

        item.setItemMeta(meta);
        return this;
    }

    public ExtendedItemStack hideAllFlags(){
        this.hideFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_UNBREAKABLE,  ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_DYE, ItemFlag.HIDE_STORED_ENCHANTS);
        return this;
    }

    public ExtendedItemStack enchant(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
        return this;
    }
    public ExtendedItemStack unbreakable() {
        return unbreakable(true);

    }

    public ExtendedItemStack unbreakable(boolean unbreakable) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return this;
    }

    public ExtendedItemStack enchantmentGlint() {
        return enchantmentGlint(true);
    }


    public ExtendedItemStack enchantmentGlint(boolean enchantmentGlint) {
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(enchantmentGlint);
        item.setItemMeta(meta);
        return this;
    }

    public ExtendedItemStack itemTag(String string){
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), string), PersistentDataType.BOOLEAN, true);

        });

        return this;
    }

    public ExtendedItemStack skullTexture(String b64Texture){
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = meta.getPlayerProfile();
        if(profile == null){
            profile = Bukkit.createProfile(UUID.randomUUID(), null);
        }
        profile.setProperty(new ProfileProperty("textures", b64Texture));
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return this;
    }

    public ExtendedItemStack hideToolTip(){
        item.setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .hideTooltip(true)   // hide the *whole* tooltip
                        .build()
        );
        return this;
    }

    public ExtendedItemStack addBannerPattern(Pattern pattern){
        if(item.getItemMeta() instanceof BannerMeta bannerMeta){
            bannerMeta.addPattern(pattern);
            item.setItemMeta(bannerMeta);
        } else if(item.getItemMeta() instanceof ShieldMeta shieldMeta){
            shieldMeta.addPattern(pattern);
            item.setItemMeta(shieldMeta);
        }
        return this;

    }

    public ExtendedItemStack setShieldBaseBanner(DyeColor color){
        if(item.getItemMeta() instanceof ShieldMeta shieldMeta){
            shieldMeta.setBaseColor(color);
            item.setItemMeta(shieldMeta);
        }
        return this;
    }

    public ExtendedItemStack resetBannerPatterns(){
        if(item.getItemMeta() instanceof BannerMeta bannerMeta){
            bannerMeta.setPatterns(List.of());
            item.setItemMeta(bannerMeta);
        } else if(item.getItemMeta() instanceof ShieldMeta shieldMeta){
            shieldMeta.setPatterns(List.of());
            item.setItemMeta(shieldMeta);
        }
        return this;
    }

    public ExtendedItemStack hideComponents(DataComponentType componentType){
        item.setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(componentType) // hide banner_patterns
                        .build()
        );
        return this;
    }

    public ExtendedItemStack customDataInt(String key, int value){
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key), PersistentDataType.INTEGER, value);
        });
        return this;

    }

    public ExtendedItemStack customDataDouble(String key, double value){
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key), PersistentDataType.DOUBLE, value);
        });
        return this;
    }

    public ExtendedItemStack customDataString(String key, String value){
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key), PersistentDataType.STRING, value);
        });
        return this;
    }

    public ExtendedItemStack customDataBoolean(String key, boolean value){
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key), PersistentDataType.BOOLEAN, value);
        });
        return this;
    }

    public ExtendedItemStack customDataLocation(String key, Location value){
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key+".world"), PersistentDataType.STRING, value.getWorld().getName());
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key+".x"), PersistentDataType.DOUBLE, value.x());
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key+".y"), PersistentDataType.DOUBLE, value.y());
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key+".z"), PersistentDataType.DOUBLE, value.z());
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key+".yaw"), PersistentDataType.FLOAT, value.getYaw());
            persistentDataContainer.set(new NamespacedKey(AdmiralPlugin.getInstance(), key+".pitch"), PersistentDataType.FLOAT, value.getPitch());
        });
        return this;
    }

    public ExtendedItemStack addAttributeModifier(Attribute attribute, AttributeModifier modifier){
        ItemMeta m = item.getItemMeta();
        m.addAttributeModifier(attribute, modifier);
        item.setItemMeta(m);
        return this;
    }


    public ExtendedItemStack equippable(EquipmentSlot slot){
        item.setData(
                DataComponentTypes.EQUIPPABLE,
                Equippable.equippable(slot).equipOnInteract(true).build()
        );
        return this;
    }
    public ExtendedItemStack equippable(Equippable equippable){
        item.setData(
                DataComponentTypes.EQUIPPABLE,
                equippable
        );
        return this;
    }

    public ItemStack get() {
        return item;
    }







}
