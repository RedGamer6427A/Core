package dev.redgamer6427a.core.minecraft.paper.configuration.values;

import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationCV extends ConfigurationValue<Location> {
    public LocationCV(String subPath, Location defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Location makeValue(Object o) {
        if(o instanceof Location l){
            return l;
        } else if (o instanceof String s){

            if (s.isEmpty()) return null;

            String[] parts = s.split(",");
            if (parts.length < 4) return null; // need at least world,x,y,z

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            float yaw = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length >= 6 ? Float.parseFloat(parts[5]) : 0f;

            return new Location(world, x, y, z, yaw, pitch);

        }
        return null;
    }
}
