package dev.redgamer6427a.core.minecraft.velocity.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import dev.redgamer6427a.core.minecraft.velocity.VelocityPlugin;

import java.util.ArrayList;
import java.util.List;


public class BrigadierCommandManager {


    private static List<BrigadierCommand> brigadierCommands = new ArrayList<>();

    /**
     * Internally queue a command
     *
     * @param brigadierCommand
     */
    public static void registerCommand(BrigadierCommand brigadierCommand) {
        brigadierCommands.add(brigadierCommand);


    }

    public static void registerAll() {
        brigadierCommands.forEach(brigadierCommand -> {
            CommandManager manager = VelocityPlugin.getInstance().getProxyServer().getCommandManager();

            CommandMeta meta = manager.metaBuilder(brigadierCommand.literal())
                    .aliases(
                            brigadierCommand.aliases().toArray(new String[0])
                    )
                    .plugin(VelocityPlugin.getInstance())
                    .build();


            manager.register(meta, new com.velocitypowered.api.command.BrigadierCommand(brigadierCommand.build()));

        });
    }





}
