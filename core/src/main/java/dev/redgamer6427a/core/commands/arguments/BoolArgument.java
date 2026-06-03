package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

import java.util.Set;

public class BoolArgument extends ArgumentNode<Boolean> {

    private final Set<String> trueValues;
    private final Set<String> falseValues;

    protected BoolArgument(String name, Set<String> trueValues, Set<String> falseValues) {
        super(null, name);
        this.trueValues = trueValues;
        this.falseValues = falseValues;
    }

    public static BoolArgument of(String name,
                                  Set<String> trueValues,
                                  Set<String> falseValues) {
        return new BoolArgument(name, trueValues, falseValues);
    }

    public static BoolArgument standard(String name) {
        return new BoolArgument(
                name,
                Set.of("true", "t", "1", "yes", "ye", "y", "positive"),
                Set.of("false", "f", "0", "no", "n", "negative")
        );
    }

    @Override
    protected ParseResult<Boolean> parse(ArgumentReader reader) throws CommandSyntaxException {
        String word = reader.readWord().toLowerCase();

        if (trueValues.contains(word)) return new ParseResult<>(1, true);
        if (falseValues.contains(word)) return new ParseResult<>(1, false);

        throw new CommandSyntaxException("Invalid boolean value: " + word);
    }
}

