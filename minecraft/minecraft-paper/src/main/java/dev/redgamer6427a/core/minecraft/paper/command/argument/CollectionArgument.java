package dev.redgamer6427a.core.minecraft.paper.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A Custom ArgumentType that allows you to have an Argument depend on a predefined Collection of Strings.
 */
public class CollectionArgument implements CustomArgumentType<String, StringArgumentType> {
    /**
     * An error message
     */
    private static final SimpleCommandExceptionType INVALID_OPTION =
            new SimpleCommandExceptionType(() -> "Invalid Option.");
    /**
     * The allowed strings the parser and suggestion build upon
     */
    private final Collection<String> validStrings;


    /**
     * Constructor
     * @param validStrings the allowed Strings
     */
    private CollectionArgument(Collection<String> validStrings ) {
        this.validStrings = validStrings;

    }

    /**
     * Factory method
     * @param validStrings the allowed Strings
     */
    public static CollectionArgument string(Collection<String> validStrings) {
        return new CollectionArgument(validStrings);
    }


    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString();

        if(validStrings.contains(input)) {
            return input;
        }
        throw INVALID_OPTION.create();

    }

    @Override
    public @NotNull ArgumentType getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull Collection<String> getExamples() {
        return List.of("key1", "key2", "key3"); // Provide examples
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        // Provide suggestions based on the validKeys collection
        for (String string : validStrings) {
            builder.suggest(string);
        }
        return builder.buildFuture();
    }

    @Override
    public String toString() {
        return "CollectionArgument: "+validStrings;
    }
}
