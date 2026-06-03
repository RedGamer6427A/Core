package dev.redgamer6427a.core.commands.arguments;

import dev.redgamer6427a.core.commands.ArgumentNode;
import dev.redgamer6427a.core.commands.ArgumentReader;
import dev.redgamer6427a.core.commands.CommandSyntaxException;
import dev.redgamer6427a.core.commands.ParseResult;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathArgument extends ArgumentNode<Path> {

    private final Path[] allowedRoots;

    private final Supplier<Path> initialPathSupplier;

    protected PathArgument(String name, Supplier<Path> initialPathSupplier, Path... allowedRoots) {
        super(null, name);
        this.initialPathSupplier = initialPathSupplier;

        this.allowedRoots = allowedRoots;
    }

    public static PathArgument restricted(String name, Supplier<Path> initialPathSupplier, Path... allowedRoots) {
        return new PathArgument(name, initialPathSupplier, allowedRoots);
    }

    public static PathArgument of(String name, Supplier<Path> initialPathSupplier) {
        return new PathArgument(name, initialPathSupplier);
    }

    @Override
    protected ParseResult<Path> parse(ArgumentReader reader) throws CommandSyntaxException {
        String read;
        int spaces = 0;
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
                spaces = countedSpaces;
                read = content;

            } else {
                throw new CommandSyntaxException("String never stops!");
            }
        } else {
            read = reader.readWord();
        }



        Path p;
        try {
             p = initialPathSupplier.get().resolve(read).normalize().toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new CommandSyntaxException("Invalid path!");
        }

        if (allowedRoots.length == 0) {
            return new ParseResult<>(1, p);
        }

        for (Path root : allowedRoots) {
            if (p.startsWith(root)) {
                return new ParseResult<>(spaces, p);
            }
        }

        throw new CommandSyntaxException("Path not allowed. Must be inside: " + Arrays.toString(allowedRoots));
    }
}

