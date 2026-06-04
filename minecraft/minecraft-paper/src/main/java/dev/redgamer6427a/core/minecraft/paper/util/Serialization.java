package dev.redgamer6427a.core.minecraft.paper.util;

import org.bukkit.Location;

public class Serialization {

    public static String location(Location location){
        return "X: " + location.getX() + ", Y: " + location.getY() + ", Z: " + location.getZ() + " in "+location.getWorld().getKey();
    }


}
