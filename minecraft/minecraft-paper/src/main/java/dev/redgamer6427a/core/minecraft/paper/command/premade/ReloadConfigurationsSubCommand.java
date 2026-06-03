package dev.redgamer6427a.admiral.paper.command.premade;

import dev.redgamer6427a.admiral.common.performance.Performance;
import dev.redgamer6427a.admiral.paper.command.AllowedSources;
import dev.redgamer6427a.admiral.paper.command.BrigadierCommand;
import dev.redgamer6427a.admiral.paper.configuration.Configuration;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Predicate;

import static dev.redgamer6427a.admiral.common.text.MiniMessageUtils.mm;

public class ReloadConfigurationsSubCommand extends BrigadierCommand {

    public ReloadConfigurationsSubCommand(String name, AllowedSources allowedSources, Predicate<CommandSourceStack> requirement) {
        super(name, "Reload Configurations");

        setDefaultExecutor(context -> {

            answer(context, mm("<green>Reloading Configurations..."));

            long millis = Performance.getCompletionMillis(() -> Configuration.getConfigurations().forEach(Configuration::load));

            answer(context, mm("<green>Reloaded Configurations! <gray><i>(" + millis + "ms)"));


        }, allowedSources, requirement);


        Configuration.getConfigurations().forEach(c -> addSubCommand(new SingleConfigurationSubCommand(c, allowedSources, requirement)));


    }

    private static class SingleConfigurationSubCommand extends BrigadierCommand {

        public SingleConfigurationSubCommand(Configuration configuration, AllowedSources allowedSources, Predicate<CommandSourceStack> requirement) {
            super(configuration.id, "Reload " + configuration.id + ".yml");

            setDefaultExecutor(context -> {

                answer(context, mm("<green>Reloading " + configuration.id + ".yml..."));

                long millis = Performance.getCompletionMillis(configuration::load);

                answer(context, mm("<green>Reloaded " + configuration.id + ".yml! <gray><i>(" + millis + "ms)"));

            }, allowedSources, requirement);

        }


    }


}
