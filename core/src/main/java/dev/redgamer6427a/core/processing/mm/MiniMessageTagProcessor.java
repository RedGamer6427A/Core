package dev.redgamer6427a.core.processing.mm;

import dev.redgamer6427a.core.console.output.ANSICode;
import dev.redgamer6427a.core.logging.Logger;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniMessageTagProcessor {

    private final List<SimpleTag> simpleTags = new ArrayList<>();
    private final List<RegexTag> regexTags = new ArrayList<>();
    private final List<ParameterTag> parameterTags = new ArrayList<>();

    private static final Logger logger = Logger.create();


    public void simpleTag(String tag, String value) {
        simpleTags.add(new SimpleTag(tag, value));
    }

    public void regexTag(String regex, Function<List<String>, String> processor) {
        regexTags.add(new RegexTag(regex, processor));
    }

    public void parameterTag(String id, int paramCount, Function<List<String>, String> withParamsProcessor, String withoutParams) {
        parameterTags.add(new ParameterTag(id, paramCount, withParamsProcessor));
        simpleTags.add(new SimpleTag(id, withoutParams));
    }


    private static Pattern parameterTagPattern = Pattern.compile("(?<!\\\\)<([^>]+)>");

    public String processAll(String message) {
        for (SimpleTag simpleTag : simpleTags) {
            message = message.replace("<" + simpleTag.tag + ">", "<" + simpleTag.value + ">");

        }

        for (RegexTag regexTag : regexTags) {
            Matcher matcher = Pattern.compile("<" + regexTag.regex + ">").matcher(message);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                List<String> values = new ArrayList<>();
                for (int i = 0; i < matcher.groupCount(); i++) {
                    values.add(matcher.group(i + 1));
                }
                String replacement = "<" + regexTag.processor.apply(values) + ">";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }

        Matcher matcher = parameterTagPattern.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String[] parts = matcher.group(1).split(":");
            String id = parts[0];
            ParameterTag parameterTag = parameterTags.stream().filter(t -> t.id.equals(id)).findFirst().orElse(null);
            if (parameterTag == null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                continue;
            }
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(parameterTag.withParamsProcessor.apply(List.of(args))));
        }
        matcher.appendTail(sb);
        message = sb.toString();

        return message;
    }

    public record SimpleTag(String tag, String value) {
    }

    public record RegexTag(String regex, Function<List<String>, String> processor) {
    }

    public record ParameterTag(String id, int paramCount, Function<List<String>, String> withParamsProcessor) {
    }


}
