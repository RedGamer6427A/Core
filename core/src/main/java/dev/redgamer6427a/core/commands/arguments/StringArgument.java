package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An ArgumentNode that accepts strings.
 */
public class StringArgument extends ArgumentNode<String> {

    /**
     * The argument type
     */
    private final ArgumentType argumentType;

    /**
     * Protected Constructor. Use factory methods instead.
     * @param name the argument's name.
     * @param argumentType the argument type.
     */
    private StringArgument(String name, ArgumentType argumentType) {
        super(null, name);
        this.argumentType = argumentType;
    }

    /**
     * Constructs a normal StringArgument.
     * @param name the argument name.
     * @return the newly constructed instance.
     */
    public static StringArgument string(String name) {
        return new StringArgument(name, ArgumentType.NORMAL);

    }

    /**
     * Constructs a greedy StringArgument. Eats up the rest of the input. You should not provide arguments after this one.
     * @param name the argument name.
     * @return the newly constructed instance.
     */
    public static StringArgument greedy(String name) {
        return new StringArgument(name, ArgumentType.GREEDY);

    }


    @Override
    protected ParseResult<String> parse(ArgumentReader reader) throws CommandSyntaxException {
        if (reader.args().size() == 1 && !reader.readWord(0).startsWith("\"")) {
            return new ParseResult<>(1, reader.readWord());
        }
        if (argumentType == ArgumentType.NORMAL) {
            if (reader.readWord(0).startsWith("\"")) {
                // Match " not preceded by an odd number of backslashes
                Pattern p = Pattern.compile("(?<!\\\\)(?:\\\\\\\\)*\"");
                Matcher m = p.matcher(reader.all());

                if (m.find(1)) { // start searching after the first quote
                    String content = reader.all().substring(1, m.start());
                    // optionally unescape \" sequences

                    int countedSpaces = 1;
                    System.out.println("content = " + content);
                    for (int i = 0; i < m.start() - 1; i++) {
                            if (content.charAt(i) == ' ') {
                                countedSpaces++;
                            }
                    }

                    return new ParseResult<>(countedSpaces, content);
                } else {
                    throw new CommandSyntaxException("String never stops!");
                }
            }
            return new ParseResult<>(1, reader.readWord());
        } else if (argumentType == ArgumentType.GREEDY) {

            int o = 0;
            for (char c : reader.all().toCharArray()) {
                if (c == ' ') {
                    o++;
                }
            }

            return new ParseResult<>(o + 1, reader.all());
        } else {
            throw new IllegalStateException("ArgumentType invalid!");
        }


    }

    /**
     * Argument type enumeration.
     */
    private enum ArgumentType {

        NORMAL,
        GREEDY,
        ;


    }

}
