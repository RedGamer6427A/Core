package dev.redgamer6427a.core.commands;

/**
 * The parsing result of an argument.
 * @param generatedOffset the amount of words the parser should jump after calling this. It matches the amount of words used by your method.
 * @param resultData the result of the parsing.
 * @param <T> the type of the result.
 */
public record ParseResult<T>(int generatedOffset, T resultData) {

}
