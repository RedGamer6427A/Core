package dev.redgamer6427a.admiral.paper.util;

import dev.redgamer6427a.admiral.common.util.AMPIntegration;
import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class PaperAMPIntegration extends AMPIntegration {

    public PaperAMPIntegration(String key) {
        super(key);
    }

    @Override
    protected void shutdown() {
        Logger l = AdmiralPlugin.getInstance().getLogger();

        l.info("Access Management Protocol denied access.");
        l.info("Reason: "+status.status());

        if(AdmiralPlugin.getInstance().getParameters().allowAMPShutDown()){
            Bukkit.getPluginManager().disablePlugin(AdmiralPlugin.getInstance());
        }

    }
}
