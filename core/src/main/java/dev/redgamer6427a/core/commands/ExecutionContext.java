package dev.redgamer6427a.core.commands;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The execution context.
 */
public class ExecutionContext {

    @Getter
    private final List<String> args;
    private final Map<ArgumentNode<?>, Object> values = new HashMap<>();
    private int ownDepth;


    public ExecutionContext(List<String> args) {
        this.args = args;
        ownDepth = 1;
    }


    /**
     * Used during parsing to figure out the next steps.
     *
     * @param argument the argument to be parsed.
     * @param <T>      the shared type between the ArgumentNode and ParseResult
     * @return the typed ParseResult.
     * @throws CommandSyntaxException when anything syntax-related goes wrong
     */
    <T> ParseResult<T> parse(ArgumentNode<T> argument) throws CommandSyntaxException {

        ArgumentReader reader = new ArgumentReader(args,
                argument.getDepth() + ownDepth);

        ParseResult<T> result = argument.parse(reader);
        values.put(argument, result.resultData());
        ownDepth += result.generatedOffset();

        return result;
    }

    /**
     * Used during execution.
     *
     * @param argument the argument to be parsed.
     * @param <T>      the shared type between the ArgumentNode and the result.
     * @return the typed object.
     * @throws CommandSyntaxException when anything syntax-related goes wrong. This should normally not be caught.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ArgumentNode<T> argument) throws CommandSyntaxException {
        if (values.containsKey(argument)) {
            return (T) values.get(argument);
        }
        return parse(argument).resultData();
    }

}
