package dev.redgamer6427a.core.processing;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    /**
     * Replaces all occurrences of a regex with a lambda processor.
     *
     * @param input     The input string
     * @param regex     Regex with one capturing group (the wildcard)
     * @param processor Lambda that receives (full match, captured value) and returns replacement
     * @return The processed string
     */
    public static String regexLambdaProcessor(String input, String regex,
                                              BiFunction<String, String, String> processor) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String fullMatch = matcher.group(0);  // the entire matched tag
            String captured = matcher.group(1);   // the captured wildcard
            String replacement = processor.apply(fullMatch, captured);
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String regexLambdaProcessorWithoutWildcard(String input, String regex,
                                                             Function<String, String> processor) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String fullMatch = matcher.group(0);  // the entire matched tag
            String replacement = processor.apply(fullMatch);
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


}
