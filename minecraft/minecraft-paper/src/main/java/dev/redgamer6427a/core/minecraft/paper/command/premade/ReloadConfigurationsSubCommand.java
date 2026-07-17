package dev.redgamer6427a.core.minecraft.paper.command.premade;

import dev.redgamer6427a.core.performance.Performance;
import dev.redgamer6427a.core.minecraft.paper.command.BrigadierCommand;
import dev.redgamer6427a.core.minecraft.paper.configuration.Configuration;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Predicate;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.mm;


public class ReloadConfigurationsSubCommand extends BrigadierCommand {

    public ReloadConfigurationsSubCommand(String name, Predicate<CommandSourceStack> requirement) {
        super(name);

        setDefaultExecutor(context -> {

            answer(context, mm("<green>Reloading Configurations..."));

            long millis = Performance.measure(() -> Configuration.getConfigurations().forEach(Configuration::load));

            answer(context, mm("<green>Reloaded Configurations! <gray><i>(" + millis + "ms)"));


        }, requirement);


        Configuration.getConfigurations().forEach(c -> addSubCommand(new SingleConfigurationSubCommand(c, requirement)));


    }

    private static class SingleConfigurationSubCommand extends BrigadierCommand {

        public SingleConfigurationSubCommand(Configuration configuration, Predicate<CommandSourceStack> requirement) {
            super(configuration.id, "Reload " + configuration.id + ".yml");

            setDefaultExecutor(context -> {

                answer(context, mm("<green>Reloading " + configuration.id + ".yml..."));

                long millis = Performance.measure(configuration::load);

                answer(context, mm("<green>Reloaded " + configuration.id + ".yml! <gray><i>(" + millis + "ms)"));

            }, requirement);

        }


    }


}
