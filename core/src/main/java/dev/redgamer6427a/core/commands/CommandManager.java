package dev.redgamer6427a.core.commands;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple Class for managing multiple commands.
 */
public abstract class CommandManager {

    private final Map<String, ConsoleCommand> commands = new HashMap<>();
    public Consumer<CommandSyntaxException> syntaxExceptionConsumer;

    public Map<String, ConsoleCommand> getCommands() {
        return Map.copyOf(commands);
    }
    public Map<String, String> commandAliases = new HashMap<>();

    /**
     *
     * @param syntaxExceptionConsumer what consumer to override the underlying commands' handler with.
     */
    protected CommandManager(Consumer<CommandSyntaxException> syntaxExceptionConsumer) {
        this.syntaxExceptionConsumer = syntaxExceptionConsumer;
    }

    /**
     * Called before a command is called
     * @param s the command used.
     * @throws CommandSyntaxException can be thrown to stop the command from execution. Use a null message to not send any error.
     * @return A modified command.
     */
    @ApiStatus.OverrideOnly
    protected String preAccept(@NotNull String s) throws CommandSyntaxException {return s;}

    /**
     * Called after a command is called
     * @param s the command used.
     */
    @ApiStatus.OverrideOnly
    protected void postAccept(@NotNull String s) {}


    /**
     * Try a command on all commands registered in this manager.
     * @param s the used command.
     */
    public void accept(@NotNull String s) {

        try {
            s = preAccept(s);
        } catch (CommandSyntaxException e) {
            if (e.getMessage() != null) {
                syntaxExceptionConsumer.accept(e);
            }
            return;

        }
        String command = s.stripLeading().split(" ")[0];

        if (command.isEmpty()) {
            return;
        }
        String alias = command;
        if (commandAliases.containsKey(command)) {
            command = commandAliases.get(command);
        }


        if (!commands.containsKey(command)) {
            syntaxExceptionConsumer.accept(new CommandSyntaxException(ConsoleCommand.invalidSyntaxMessage));
            return;
        }
        commands.get(command).accept(s.replaceFirst(alias, command));

        postAccept(s);
    }

    /**
     * Register a command into this manager.
     * @param command the command to register.
     */
    protected void addCommand(ConsoleCommand command) {
        if (commands.containsKey(command.getName())) {
            throw new IllegalArgumentException("A command with the name" + command.getName() + " already exists in this manager.");
        }

        commands.put(command.getName(), command);

        command.setSyntaxExceptionConsumer(this.syntaxExceptionConsumer);

    }

    /**
     * Register a command into this manager.
     * @param command the command to register.
     */
    protected void addCommand(ConsoleCommand command, String... aliases) {

        for (String alias : aliases) {
            this.commandAliases.put(alias, command.getName());
        }
        addCommand(command);

    }
}
