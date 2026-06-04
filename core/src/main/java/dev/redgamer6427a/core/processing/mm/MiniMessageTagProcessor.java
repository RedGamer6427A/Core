package dev.redgamer6427a.core.processing.mm;

import dev.redgamer6427a.core.console.output.ANSICode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MiniMessageTagProcessor {

    private final List<SimpleTag> simpleTags = new ArrayList<>();
    private final List<RegexTag> regexTags = new ArrayList<>();
    private final List<ParameterTag> parameterTags = new ArrayList<>();


    protected void simpleTag(String tag, String value) {
        simpleTags.add(new SimpleTag(tag, value));
    }

    protected void regexTag(String regex, Function<List<String>, String> processor){
        regexTags.add(new RegexTag(regex, processor));
    }

    protected void parameterTag(String id, int paramCount, Function<List<String>, String> withParamsProcessor, String withoutParams) {
        parameterTags.add(new ParameterTag(id, paramCount, withParamsProcessor));
        simpleTags.add(new SimpleTag(id, withoutParams));
    }

    private static Pattern parameterTagPattern = Pattern.compile("(?<!\\\\)<([^>]+)>");

    public String processAll(String message){
        for (SimpleTag simpleTag : simpleTags) {
            message = message.replace("<"+simpleTag.tag+">", "<"+simpleTag.value+">");

        }

        for (RegexTag regexTag : regexTags) {
            Matcher matcher = Pattern.compile("<"+regexTag.regex+">").matcher(message);

            while(matcher.find()) {
                List<String> values = new ArrayList<>();
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String group = matcher.group(i+1);
                    values.add(group);

                }
                message = message.replace(matcher.group(0), "<"+regexTag.processor.apply(values)+">");
            }

        }
        Matcher matcher = parameterTagPattern.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String[] parts = matcher.group(1).split(",");
            String id = parts[0];
            ParameterTag parameterTag = parameterTags.stream().filter(t -> t.id.equals(id)).findFirst().orElse(null);
            if (parameterTag == null) {
                matcher.appendReplacement(sb, matcher.group());
                continue;
            }
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(parameterTag.withParamsProcessor.apply(List.of(args))));
        }

            return message;
    }

    public record SimpleTag(String tag, String value){}

    public record RegexTag(String regex, Function<List<String>, String> processor){}

    public record ParameterTag(String id, int paramCount, Function<List<String>, String> withParamsProcessor){}



}
