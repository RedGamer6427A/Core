package dev.redgamer6427a.core.minecraft.paper.util;

import dev.redgamer6427a.core.minecraft.common.util.FunnyAMPIntegration;
import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class FunnyPaperAMPIntegration extends FunnyAMPIntegration {

    public FunnyPaperAMPIntegration(String key) {
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
