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
     * @param binding the argument binding to be parsed (carries its own registration-specific depth).
     * @param <T>      the shared type between the ArgumentNode and ParseResult
     * @return the typed ParseResult.
     * @throws CommandSyntaxException when anything syntax-related goes wrong
     */
    <T> ParseResult<T> parse(ArgumentBinding<T> binding) throws CommandSyntaxException {

        ArgumentReader reader = new ArgumentReader(args,
                binding.getDepth() + ownDepth);

        ParseResult<T> result = binding.getNode().parse(reader);
        values.put(binding.getNode(), result.resultData());
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
        // BUG FIX (framework): previously fell back to parse(argument), which relied on
        // depth living on the node itself. That's exactly what turned a "wrong executor
        // fired" bug into an ArrayIndexOutOfBoundsException/CommandSyntaxException crash —
        // an argument from an unrelated syntax path (e.g. quickstart's projectArgument
        // reached from start's executor) got parsed with meaningless depth math. Now that
        // depth only exists on ArgumentBinding (per-registration), there's no safe way to
        // parse an argument here without knowing which binding it belongs to. Fail loudly
        // and clearly instead of silently corrupting the read position.
        throw new IllegalStateException(
                "Argument '" + argument.getName() + "' was not parsed on the matched syntax path. " +
                "context.get() can only retrieve arguments that are part of the command's own executed syntax."
        );
    }

}
