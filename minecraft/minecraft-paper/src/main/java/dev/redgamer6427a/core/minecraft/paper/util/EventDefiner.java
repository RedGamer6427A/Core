package dev.redgamer6427a.core.minecraft.paper.util;

import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import org.bukkit.event.Listener;

public final class EventDefiner {

    public void define(Listener listener){
        PaperPlugin.getInstance().getServer().getPluginManager().registerEvents(listener, PaperPlugin.getInstance());


    }


}
