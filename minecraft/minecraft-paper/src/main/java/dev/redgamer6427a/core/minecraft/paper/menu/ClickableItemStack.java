package dev.redgamer6427a.admiral.paper.menu;

import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import dev.redgamer6427a.admiral.paper.item.ExtendedItemStack;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.redgamer6427a.admiral.common.text.MiniMessageUtils.mm;
import static org.bukkit.Material.BARRIER;

public class ClickableItemStack extends ItemStack {

    private final Consumer<InventoryClickEvent> eventHandler;

    private final Sound sound;

    static NamespacedKey key = new NamespacedKey(AdmiralPlugin.getInstance(), "menuitem");

    public static Map<String, ClickableItemStack> registerMap = new HashMap<>();

    private ClickableItemStack(Material material, int count, Consumer<InventoryClickEvent> eventHandler){
        super(material, count);
        this.eventHandler = eventHandler;
        this.sound = null;
        registerMap.put(UUID.randomUUID().toString(), this);

    }
    private ClickableItemStack(ItemStack itemStack, Consumer<InventoryClickEvent> eventHandler){

        super(itemStack);
        sound = null;
        this.eventHandler = eventHandler;
        String uuid = UUID.randomUUID().toString();
        this.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid);
        });
        registerMap.put(uuid, this);

    }

    private ClickableItemStack(Material material, int count, Consumer<InventoryClickEvent> eventHandler, Sound sound){
        super(material, count);
        this.eventHandler = eventHandler;
        sound = null;
        this.sound = sound;
        registerMap.put(UUID.randomUUID().toString(), this);

    }
    private ClickableItemStack(ItemStack itemStack, Consumer<InventoryClickEvent> eventHandler, Sound sound){

        super(itemStack);
        this.eventHandler = eventHandler;
        this.sound = sound;
        String uuid = UUID.randomUUID().toString();
        this.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid);
        });
        registerMap.put(uuid, this);

    }

    public static ClickableItemStack item(ItemStack itemStack, Consumer<InventoryClickEvent> eventHandler){
        return new ClickableItemStack(itemStack, eventHandler);
    }

    public static ClickableItemStack item(ItemStack itemStack, Consumer<InventoryClickEvent> eventHandler, Sound sound){
        return new ClickableItemStack(itemStack, eventHandler, sound);
    }

    public static ClickableItemStack closeItem(){
        return new ClickableItemStack(new ExtendedItemStack(BARRIER).itemName(mm("<red>Close")).get(), inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            inventoryClickEvent.setResult(Event.Result.DENY);
            inventoryClickEvent.getClickedInventory().close();
        });
    }

    public static ClickableItemStack closeItem(Sound sound){
        return new ClickableItemStack(new ExtendedItemStack(BARRIER).itemName(mm("<red>Close")).get(), inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            inventoryClickEvent.setResult(Event.Result.DENY);
            inventoryClickEvent.getClickedInventory().close();
        }, sound);
    }


    public void handle(InventoryClickEvent event){
        if(sound != null){
            event.getWhoClicked().playSound(sound);
        }
        eventHandler.accept(event);
    }

    public static Sound DEFAULT_SOUND_TRAVEL(){
        return Sound.sound(org.bukkit.Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, Sound.Source.MASTER, 1.0f, 2.0f);
    }
    public static Sound DEFAULT_SOUND_DESTINATION(){
        return Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1.0f, 1.0f);
    }
    public static Sound DEFAULT_SOUND_ERROR(){
        return Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_BIT, Sound.Source.MASTER, 1.0f, 0.0f);
    }


}
