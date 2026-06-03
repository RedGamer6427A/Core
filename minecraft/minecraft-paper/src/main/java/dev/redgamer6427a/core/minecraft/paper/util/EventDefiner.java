package dev.redgamer6427a.admiral.paper.util;

import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import org.bukkit.event.Listener;

public final class EventDefiner {

    public void define(Listener listener){
        AdmiralPlugin.getInstance().getServer().getPluginManager().registerEvents(listener, AdmiralPlugin.getInstance());


    }


}
