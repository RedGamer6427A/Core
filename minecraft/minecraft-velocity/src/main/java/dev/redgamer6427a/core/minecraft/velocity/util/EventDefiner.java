package dev.redgamer6427a.core.minecraft.velocity.util;

import dev.redgamer6427a.core.minecraft.velocity.VelocityPlugin;

public final class EventDefiner {

    public void define(Object listener){
        VelocityPlugin.getInstance().getProxyServer().getEventManager().unregisterListener(VelocityPlugin.getInstance(), listener);


    }


}
