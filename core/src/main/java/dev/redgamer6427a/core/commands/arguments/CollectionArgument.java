package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

import java.util.Map;
import java.util.function.Supplier;

public class CollectionArgument<T> extends ArgumentNode<T> {

    private final Supplier<Map<String, T>> lookup;

    protected CollectionArgument(String name, Supplier<Map<String, T>> allowed) {
        super(null, name);
        this.lookup = allowed;
    }

    public static <T> CollectionArgument<T> of(String name, Supplier<Map<String, T>> allowed) {
        return new CollectionArgument<>(name, allowed);
    }

    @Override
    protected ParseResult<T> parse(ArgumentReader reader) throws CommandSyntaxException {
        String word = reader.readWord().toLowerCase();
        T value = lookup.get().get(word);
        if (value == null) {
            throw new CommandSyntaxException("Invalid value for " + getName() + ".");
        }

        return new ParseResult<>(1, value);
    }
}

