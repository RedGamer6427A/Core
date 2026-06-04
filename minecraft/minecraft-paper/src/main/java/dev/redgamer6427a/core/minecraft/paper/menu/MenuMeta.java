package dev.redgamer6427a.core.minecraft.paper.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MenuMeta {

    private final int rows;
    private final Component title;

    public MenuMeta(int rows, Component title){
        if(rows > 6){
            throw new IllegalArgumentException("Minecraft does not allow for inventories bigger than 6 rows.");
        }
        if(rows < 1){
            throw new IllegalArgumentException("Minecraft does not allow for inventories smaller than 1 row.");
        }
        this.rows = rows;
        this.title = title;
    }
    public MenuMeta(int rows, String title){
        this.rows = rows;
        this.title = MiniMessage.miniMessage().deserialize(title);
    }

    public int rows() {
        return rows;
    }
    public Component title() {
        return title;
    }
    public String stringTitle(){
        return MiniMessage.miniMessage().serialize(title);
    }


}
