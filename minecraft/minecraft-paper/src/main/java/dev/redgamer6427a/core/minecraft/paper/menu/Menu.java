package dev.redgamer6427a.core.minecraft.paper.menu;

import dev.redgamer6427a.core.minecraft.paper.item.ExtendedItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMiniMessage.mm;


public abstract class Menu implements InventoryHolder {
    protected Inventory inventory;
    protected Player player;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void clickHandler(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    public abstract MenuMeta getMeta();

    public Menu(Player player){
        this.player = player;
        inventory = Bukkit.createInventory(
                this,
                getMeta().rows() * 9,
                getMeta().title()
        );
        open();


    }

    public Menu(Player player, boolean delayOpen){
        this.player = player;
        inventory = Bukkit.createInventory(
                this,
                getMeta().rows() * 9,
                getMeta().title()
        );
        if(!delayOpen){
            open();
        }


    }

    protected void open(){
        defineContents();
        player.openInventory(inventory);
    }

    public abstract void defineContents();

    protected void fillInEmpty(ItemStack itemStack){
        for(int i = 0; i < getMeta().rows() * 9; i++){
            if(inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR){
                inventory.setItem(i, itemStack);
            }
        }

    }

    protected void fillSlots(ItemStack itemStack, int slotA, int slotB){

        for(int i = slotA; i < slotB+1; i++){
            inventory.setItem(i, itemStack);

        }

    }

    public static ItemStack filler(){
        return new ExtendedItemStack(Material.BLACK_STAINED_GLASS_PANE).itemName(mm("")).hideToolTip().get();
    }

}

