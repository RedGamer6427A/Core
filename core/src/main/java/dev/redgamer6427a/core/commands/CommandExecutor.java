package dev.redgamer6427a.core.commands;

/**
 * Custom FunctionalInterface because why not.
 */
@FunctionalInterface
public interface CommandExecutor {

    void execute(ExecutionContext context) throws CommandSyntaxException;

}
