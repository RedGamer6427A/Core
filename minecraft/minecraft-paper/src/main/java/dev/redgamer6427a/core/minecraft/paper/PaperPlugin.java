package dev.redgamer6427a.core.minecraft.paper;

import dev.redgamer6427a.core.console.output.ConsoleMiniMessage;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommandManager;
import dev.redgamer6427a.core.minecraft.paper.menu.MenuEventHandler;
import dev.redgamer6427a.core.minecraft.paper.protocol.PacketInterceptor;
import dev.redgamer6427a.core.minecraft.paper.util.EventDefiner;
import dev.redgamer6427a.core.performance.Performance;
import lombok.Getter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;

import static dev.redgamer6427a.core.console.output.ConsoleMiniMessage.mm;

public abstract class PaperPlugin extends JavaPlugin {

    static @Getter PaperPlugin instance;

    public static ComponentLogger logger(){

        if (instance == null){
            throw new IllegalStateException("Plugin is null but AdmiralPlugin#logger was called");
        }
        return instance.getComponentLogger();

    }
    private static final Logger logger = Logger.create();

    @Override
    public final void onDisable() {

        long m = Performance.measure(() -> {
            PacketInterceptor.disable();
            disable();

        });
        logger().info(mm("Shut down. (" + m + "ms)"));
        instance = null;
        // execute any code here? die.
    }


    public abstract String getVerboseAnswerPermission();

    @Override
    public final void onEnable() {
        long m = Performance.measure(() -> {

            AdventureMM.mm("initialize colors!"); // hmm yes I'm so good at thread stuff!

            if (instance != null) {
                logger.error(mm("Something went severely wrong."));

            }
            instance = this;


            ConsoleMiniMessage.initDefaultColors();
            AdventureMM.buildParser();
            beforeEnable();

            defineConfigurations();

            EventDefiner eventDefiner = new EventDefiner();

            eventDefiner.define(new MenuEventHandler());
            eventDefiner.define(new PacketInterceptor());
            defineEvents(eventDefiner);

            defineCommands();
            BrigadierCommandManager.processQueue();

            afterEnable();
        });

        logger().info(mm("Done. (" + m + "ms)"));

    }

    public void defineCommands(){}

    public void defineEvents(EventDefiner eventDefiner){}

    public void defineConfigurations(){}

    public void beforeEnable(){}

    public void afterEnable(){}

    public void disable(){}

}
