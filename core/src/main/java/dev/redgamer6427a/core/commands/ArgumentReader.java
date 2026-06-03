package dev.redgamer6427a.core.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Objects;

/**
 * Facilitates parsing.
 */
public final class ArgumentReader {

    private final List<String> args;
    private final @Range(from = 0, to = Integer.MAX_VALUE) int index;

    /**
     * Constructor
     * @param args all the arguments.
     * @param index the index at which the parsing is currently situated.
     */
    public ArgumentReader(@NotNull List<String> args, @Range(from = 0, to = Integer.MAX_VALUE) int index) {
        this.args = args.stream().toList();
        this.index = index;

    }

    private int currentOffset = 0;

    /**
     * Read a word at a certain offset relative to the parsing process.
     * @param offset the offset.
     * @return the word at that position.
     */
    public String readWord(@Range(from = 0, to = Integer.MAX_VALUE) int offset) {
        
        
        return args.get(index + offset);

    }

    /**
     * Read a word at a certain offset relative to the parsing process. This offset will increase each time this is called.
     * @return the word at that position.
     */
    public String readWord() {
        currentOffset++;
        return readWord(currentOffset-1);
    }

    public List<String> args() {
        return args;
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int index() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ArgumentReader) obj;
        return Objects.equals(this.args, that.args) &&
                this.index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(args, index);
    }

    @Override
    public String toString() {
        return "ArgumentReader[" +
                "args=" + args + ", " +
                "index=" + index + ']';
    }

    /**
     *
     * @return roughly the data the user provided.
     */
    public String all(){
        return String.join(" ", args.subList(index, args.size()));

    }

    /**
     *
     * @return roughly the data the user from a certain word on.
     */
    public String allFrom(int offset){
        return String.join(" ", args.subList(offset, args.size()));
    }


}
