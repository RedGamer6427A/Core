package dev.redgamer6427a.core.processing.mm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MiniMessageParser {

    private final List<MiniMessageTagProcessor> tagProcessors = new ArrayList<>();

    public void addProcessor(MiniMessageTagProcessor processor) {
        tagProcessors.add(processor);
    }

    public String parse(String message) {

        for (MiniMessageTagProcessor tagProcessor : tagProcessors) {
            message = tagProcessor.processAll(message);
        }
        return message;
    }


}
