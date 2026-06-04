package dev.redgamer6427a.core.minecraft.paper.menu.pagedMenu;

import dev.redgamer6427a.core.minecraft.paper.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PagedMenu<T> extends Menu {

    public final int page;

    public abstract List<T> processableItems();

    public abstract void setPermanentParts();

    public boolean canGoBack(){
        return page > 0;

    }

    public int maxItemsPerPage(){
        int out = 0;
        for(Map.Entry<Integer, Integer> entry : getMeta().mutableRanges().entrySet()){
            out += entry.getValue() - entry.getKey();
            out++;
        }
        return out;
    }

    public List<Integer> getMutableSlots() {
        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : getMeta().mutableRanges().entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();

            // Ensure correct direction
            if (start <= end) {
                for (int i = start; i <= end; i++) {
                    out.add(i);
                }
            } else {
                for (int i = start; i >= end; i--) {
                    out.add(i);
                }
            }
        }
        return out;
    }


    public boolean canGoForward(){
        return itemsOnThisPage == maxItemsPerPage() && processableItems().size() % maxItemsPerPage() != 0;
    }

    public PagedMenu(Player player) {
        super(player, true);
        this.page = 0;
        open();
    }

    public PagedMenu(Player player, int page) {
        super(player, true);
        this.page = page;
        open();
    }

    protected abstract ItemStack processItem(T item);

    @Override
    public abstract PagedMenuMeta getMeta();

    protected int itemsOnThisPage = 0;

    @Override
    public void defineContents() {
        int itemInList = page*maxItemsPerPage();
        player.sendMessage("- "+maxItemsPerPage());
        for(int i = 0; i < maxItemsPerPage(); i++){
            if(processableItems().size() <= itemInList){
                break;
            }
            ItemStack item = processItem(processableItems().get(itemInList));
            if(item != null){
                player.sendMessage(""+i);
                inventory.setItem(getMutableSlots().get(i), item);
                itemsOnThisPage++;
            } else {
                i--;
            }
            itemInList++;

        }
        setPermanentParts();
    }
}
