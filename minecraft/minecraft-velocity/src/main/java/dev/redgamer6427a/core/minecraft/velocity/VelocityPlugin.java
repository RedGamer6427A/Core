package dev.redgamer6427a.core.minecraft.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.redgamer6427a.core.console.output.ConsoleMiniMessage;
import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.velocity.command.BrigadierCommandManager;
import dev.redgamer6427a.core.minecraft.velocity.util.EventDefiner;
import dev.redgamer6427a.core.performance.Performance;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static dev.redgamer6427a.core.console.output.ConsoleMiniMessage.mm;

@Slf4j
public abstract class VelocityPlugin {

    @Getter
    private static VelocityPlugin instance;

    @Getter
    protected final ProxyServer proxyServer;
    @Getter
    protected final java.util.logging.Logger javaLogger;
    @Getter
    protected final ComponentLogger componentLogger;
    @Getter
    protected final Path dataDirectory;
    @Getter
    protected final PluginDescription pluginDescription;
    @Getter
    protected final PluginContainer pluginContainer;
    @Getter
    protected final ExecutorService executorService;

    public abstract String getVerboseAnswerPermission();

    private static final Logger logger = Logger.create();




    public VelocityPlugin(ProxyServer proxyServer, java.util.logging.Logger javaLogger, ComponentLogger componentLogger, @DataDirectory Path dataDirectory, PluginDescription pluginDescription, PluginContainer pluginContainer, ExecutorService executorService) {
        Logger.setOut(javaLogger::info);
        Logger.setErrOut(javaLogger::severe);

        if (instance != null) {
            logger.error("Something went severely wrong.");

        }
        instance = this;

        this.proxyServer = proxyServer;
        this.javaLogger = javaLogger;
        this.componentLogger = componentLogger;
        this.dataDirectory = dataDirectory;
        this.pluginDescription = pluginDescription;
        this.pluginContainer = pluginContainer;
        this.executorService = executorService;


    }
    @Subscribe
    public final void onDisable(ProxyShutdownEvent event) {
        long m = Performance.measure(this::disable);
        componentLogger.info(mm("Shut down. (" + m + "ms)"));
        instance = null;
    }

    @Subscribe
    public final void onEnable(ProxyInitializeEvent event) {
        long m = Performance.measure(() -> {

            AdventureMM.mm("initialize colors!"); // hmm yes I'm so good at thread stuff!



            ConsoleMiniMessage.initDefaultColors();
            AdventureMM.buildParser();
            beforeEnable();

            defineConfigurations();

            EventDefiner eventDefiner = new EventDefiner();
            eventDefiner.define(this);
            defineEvents(eventDefiner);

            defineCommands();
            BrigadierCommandManager.registerAll();

            afterEnable();
        });

        logger.info("Done. (" + m + "ms)");

    }

    public void defineCommands(){}

    public void defineEvents(EventDefiner eventDefiner){}

    public void defineConfigurations(){}

    public void beforeEnable(){}

    public void afterEnable(){}

    public void disable(){}

}
