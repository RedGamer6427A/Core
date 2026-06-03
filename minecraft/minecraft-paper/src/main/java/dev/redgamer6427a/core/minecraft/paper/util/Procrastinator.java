package dev.redgamer6427a.admiral.paper.util;

import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class Procrastinator {

    public static BukkitTask later(int ticks, Runnable runnable) {
        return Bukkit.getScheduler().runTaskLater(AdmiralPlugin.getInstance(), runnable, ticks);
    }

    public static BukkitTask repeat(long intervalTicks, Runnable task) {
        return Bukkit.getScheduler().runTaskTimer(AdmiralPlugin.getInstance(), task, 0L, intervalTicks);
    }

    // repeat with an initial delay
    public static BukkitTask repeat(long delayTicks, long intervalTicks, Runnable task) {
        return Bukkit.getScheduler().runTaskTimer(AdmiralPlugin.getInstance(), task, delayTicks, intervalTicks);
    }

}
