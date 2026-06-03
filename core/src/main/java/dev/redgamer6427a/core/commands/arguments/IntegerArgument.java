package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

/**
 * An ArgumentNode that accepts integers.
 */
public class IntegerArgument extends ArgumentNode<Integer> {

    /**
     * Inclusive
     */
    final int minRange;
    /**
     * Inclusive
     */
    final int maxRange;

    /**
     * Protected Constructor. Use factory methods instead.
     *
     * @param name     the argument's name.
     * @param minRange the minimum that the input has to match or be above.
     * @param maxRange the maximum that the input has to match or be below.
     */
    protected IntegerArgument(String name, int minRange, int maxRange) {
        super(null, name);
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    /**
     * Factory method for a ranged IntegerArgument.
     *
     * @param name     the argument's name.
     * @param minRange the minimum value, inclusive.
     * @param maxRange the maximum value, inclusive.
     * @return the newly constructed IntegerArgument.
     */
    public static IntegerArgument ranged(String name, int minRange, int maxRange) {
        return new IntegerArgument(name, minRange, maxRange);
    }


    /**
     * Factory method for a ranged IntegerArgument.
     *
     * @param name the argument's name.
     * @return the newly constructed IntegerArgument.
     */
    public static IntegerArgument integer(String name) {
        return new IntegerArgument(name, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }


    @Override
    protected ParseResult<Integer> parse(ArgumentReader reader) throws CommandSyntaxException {
        String word = reader.readWord();
        int r;
        try {
            r = Integer.parseInt(word);
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(word);
                throw new CommandSyntaxException("Only whole are numbers allowed!");
            } catch (NumberFormatException e1) {
                throw new CommandSyntaxException("Invalid Integer.");
            }


        }
        if (r < minRange) {
            throw new CommandSyntaxException("Number is too low! Minimum is " + minRange + ".");

        } else if (r > maxRange) {
            throw new CommandSyntaxException("Number is too high! Maximum is " + maxRange + ".");
        }
        return new ParseResult<>(1, r);
    }
}
