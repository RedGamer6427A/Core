package dev.redgamer6427a.core.commands;

/**
 * Custom Exception because why not.
 */
public class CommandSyntaxException extends Exception {
    public CommandSyntaxException(String message) {
        super(message);
    }
}
