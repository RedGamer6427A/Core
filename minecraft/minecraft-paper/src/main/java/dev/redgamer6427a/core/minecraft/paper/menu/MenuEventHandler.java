package dev.redgamer6427a.core.minecraft.paper.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;


public class MenuEventHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void menuClick(InventoryClickEvent event) {
        if(event.getClickedInventory() != null && event.getCurrentItem() != null) {

            if(event.getClickedInventory().getHolder() instanceof Menu menu){

                menu.clickHandler(event);
            }
            if(event.getCurrentItem().getItemMeta() != null){
                if(ClickableItemStack.registerMap.containsKey(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(ClickableItemStack.key, PersistentDataType.STRING))){
                    ClickableItemStack itemStack = ClickableItemStack.registerMap.get(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(ClickableItemStack.key, PersistentDataType.STRING));

                    itemStack.handle(event);
                }
            }
        }

    }

}
