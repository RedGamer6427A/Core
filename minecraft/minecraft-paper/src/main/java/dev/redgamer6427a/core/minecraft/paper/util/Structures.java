package dev.redgamer6427a.admiral.paper.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;

import java.util.Random;

public class Structures {

    public static boolean loadStructure(Location location, String name) {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey(name.split(":")[0], name.split(":")[1]));
        if(structure == null) return false;
        System.out.println(structure.getSize());
        structure.place(location, false, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, new Random());
        return true;
    }


}
