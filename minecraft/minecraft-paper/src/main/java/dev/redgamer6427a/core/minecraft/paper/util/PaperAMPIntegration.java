package dev.redgamer6427a.core.minecraft.paper.util;

import dev.redgamer6427a.core.minecraft.common.util.AMPIntegration;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class PaperAMPIntegration extends AMPIntegration {

    public PaperAMPIntegration(String key) {
        super(key);
    }

    @Override
    protected void shutdown() {
        Logger l = PaperPlugin.getInstance().getLogger();

        l.info("Access Management Protocol denied access.");
        l.info("Reason: "+status.status());

        if(PaperPlugin.getInstance().getParameters().allowAMPShutDown()){
            Bukkit.getPluginManager().disablePlugin(PaperPlugin.getInstance());
        }

    }
}
