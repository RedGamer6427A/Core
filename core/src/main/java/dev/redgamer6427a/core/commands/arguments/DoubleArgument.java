package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

/**
 * An ArgumentNode that accepts doubles.
 */
public class DoubleArgument extends ArgumentNode<Double> {

    /**
     * Inclusive
     */
    final double minRange;
    /**
     * Inclusive
     */
    final double maxRange;

    /**
     * Protected Constructor. Use factory methods instead.
     *
     * @param name     the argument's name.
     * @param minRange the minimum that the input has to match or be above.
     * @param maxRange the maximum that the input has to match or be below.
     */
    protected DoubleArgument(String name, double minRange, double maxRange) {
        super(null, name);
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    /**
     * Factory method for a ranged DoubleArgument.
     *
     * @param name     the argument's name.
     * @param minRange the minimum value, inclusive.
     * @param maxRange the maximum value, inclusive.
     * @return the newly constructed DoubleArgument.
     */
    public static DoubleArgument ranged(String name, double minRange, double maxRange) {
        return new DoubleArgument(name, minRange, maxRange);
    }


    /**
     * Factory method for a ranged DoubleArgument.
     *
     * @param name the argument's name.
     * @return the newly constructed DoubleArgument.
     */
    public static DoubleArgument arg(String name) {
        return new DoubleArgument(name, Double.MIN_VALUE, Double.MAX_VALUE);
    }


    @Override
    protected ParseResult<Double> parse(ArgumentReader reader) throws CommandSyntaxException {
        String word = reader.readWord();
        double r;
        try {
            r = Double.parseDouble(word);
        } catch (NumberFormatException e) {
            throw new CommandSyntaxException("Invalid Double.");

        }
        if (r < minRange) {
            throw new CommandSyntaxException("Number is too low! Minimum is " + minRange + ".");

        } else if (r > maxRange) {
            throw new CommandSyntaxException("Number is too high! Maximum is " + maxRange + ".");
        }
        return new ParseResult<>(1, r);
    }
}
