package dev.redgamer6427a.admiral.paper.command;

import com.mojang.brigadier.tree.CommandNode;
import dev.redgamer6427a.admiral.paper.AdmiralPlugin;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BrigadierCommandManager {
    /**
     * All in queue commands.
     */
    private static List<BrigadierCommand> commands = new ArrayList<>();
    /**
     * Internally queue a context
     *
     * @param brigadierCommand
     */
    public static void queueCommandRegistration(BrigadierCommand brigadierCommand) {
        commands.add(brigadierCommand);

    }


    public static void processQueue(){

        LifecycleEventManager<@NotNull Plugin> manager = AdmiralPlugin.getInstance().getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            for (BrigadierCommand command : commands) {
                registrar.register(command.getNode(), command.getDescription(), command.getAliases());
            }
        });

    }

    private static void printTree(CommandNode<?> node) {
        printTree(node, "", true);
    }

    private static void printTree(CommandNode<?> node, String prefix, boolean isTail) {
        // Print the current node name
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.getName());

        // Convert children to a list so we can detect the last one
        var children = node.getChildren().stream().toList();
        for (int i = 0; i < children.size(); i++) {
            boolean last = i == children.size() - 1;
            printTree(children.get(i), prefix + (isTail ? "    " : "│   "), last);
        }
    }

}
