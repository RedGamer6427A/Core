package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

import java.util.HashMap;
import java.util.Map;

public class EnumArgument<E extends Enum<E>> extends ArgumentNode<E> {

    private final Map<String, E> lookup = new HashMap<>();

    protected EnumArgument(Class<E> enumClass, String name, Map<String, E> aliases) {
        super(null, name);

        // Register enum names
        for (E constant : enumClass.getEnumConstants()) {
            lookup.put(constant.name().toLowerCase(), constant);
        }

        // Register aliases
        if (aliases != null) {
            aliases.forEach((alias, target) -> lookup.put(alias.toLowerCase(), target));
        }
    }

    public static <E extends Enum<E>> EnumArgument<E> of(
            Class<E> enumClass,
            String name,
            Map<String, E> aliases
    ) {
        return new EnumArgument<>(enumClass, name, aliases);
    }

    public static <E extends Enum<E>> EnumArgument<E> of(Class<E> enumClass, String name) {
        return new EnumArgument<>(enumClass, name, null);
    }

    @Override
    protected ParseResult<E> parse(ArgumentReader reader) throws CommandSyntaxException {
        String word = reader.readWord().toLowerCase();

        E val = lookup.get(word);
        if (val == null) {
            throw new CommandSyntaxException("Invalid value for " + getName() + ".");
        }

        return new ParseResult<>(1, val);
    }
}
