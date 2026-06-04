package dev.redgamer6427a.core.minecraft.paper.player;

import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;


public class ExtendedPlayer {

    public static List<Class<? extends PlayerModule>> registeredModuleClasses = new ArrayList<>();
    @Getter
    final private Player player;
    private final List<PlayerModule> ownModules;

    private static final Map<UUID, ExtendedPlayer> instances = new HashMap<>();

    private ExtendedPlayer(Player player) {
        this.player = player;
        ownModules = new ArrayList<>();
        for (Class<? extends PlayerModule> registeredModuleClass : registeredModuleClasses) {
            try {
                ownModules.add(registeredModuleClass.getDeclaredConstructor(ExtendedPlayer.class).newInstance(this));
            } catch (ReflectiveOperationException e) {
                PaperPlugin.logger()
                        .error("Error while instantiating {} for ExtendedPlayer", registeredModuleClass.getName());
                throw new RuntimeException(e);
            }
        }
    }

    public static ExtendedPlayer of(Player player) {

        if (!instances.containsKey(player.getUniqueId())) {
            instances.put(player.getUniqueId(), new ExtendedPlayer(player));
        }
        return instances.get(player.getUniqueId());

    }

    public static void registerModule(Class<? extends PlayerModule> moduleClass) {
        registeredModuleClasses.add(moduleClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerModule> T getModule(Class<T> moduleClass) {
        for (PlayerModule playerModule : ownModules) {
            if (moduleClass.isAssignableFrom(playerModule.getClass())) {
                return (T) playerModule;
            }
        }
        throw new IllegalStateException("A module could not be found for " + moduleClass.getName());
    }


}
