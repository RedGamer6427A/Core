package dev.redgamer6427a.core.console.tui.core;

import dev.redgamer6427a.core.console.ANSIUtil;
import dev.redgamer6427a.core.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.redgamer6427a.core.console.output.ConsoleMiniMessage.mm;

/**
 * A 2D character buffer for building terminal UIs.
 *
 * <p>Stores a grid of characters and their associated ANSI escape codes separately,
 * then serializes to a positioned ANSI string via {@link #render(int, int)}.
 *
 * <p>Most write methods advance the cursor after writing, supporting chained calls.
 * The cursor wraps horizontally: writing past {@code width} increments {@code cursorY}.
 *
 * <p>Coordinates are 0-based internally. {@link #render(int, int)} takes 1-based
 * terminal coordinates for the top-left corner of the output region.
 *
 * <p>Example:
 * <pre>{@code
 * TerminalBuilder tb = new TerminalBuilder(80, 24);
 * tb.cursor(0, 0).appendMini("<red>Hello</red> world");
 * System.out.print(tb.render(1, 1));
 * }</pre>
 */
@Getter
@Setter
public class TerminalBuilder {

    private static final Logger logger = Logger.create();
    private final int width;
    private final int height;
    /**
     * Current cursor X position (0-based column).
     */
    private int cursorX = 0;
    /**
     * Current cursor Y position (0-based row).
     */
    private int cursorY = 0;
    /**
     * Character grid. {@code contents.get(row).get(col)} is the character at (col, row).
     * Initialized to spaces.
     */
    private List<List<Character>> contents = new ArrayList<>();
    /**
     * ANSI escape code grid, parallel to {@code contents}.
     * Each cell holds escape sequences that precede the character at that position.
     */
    private List<List<StringBuilder>> escapes = new ArrayList<>();

    /**
     * Creates a new {@code TerminalBuilder} with the given dimensions.
     * All cells are initialized to {@code ' '} with no escape codes.
     *
     * @param width  number of columns
     * @param height number of rows
     */
    public TerminalBuilder(int width, int height) {
        this.width = width;
        this.height = height;

        for (int i = 0; i < height; i++) {
            List<Character> row = new ArrayList<>(width);
            List<StringBuilder> escRow = new ArrayList<>(width);
            for (int j = 0; j < width; j++) {
                row.add(' ');
                escRow.add(new StringBuilder());
            }
            contents.add(row);
            escapes.add(escRow);
        }
    }

    // -------------------------------------------------------------------------
    // Cursor movement
    // -------------------------------------------------------------------------

    /**
     * Sets the cursor to an absolute position.
     *
     * @param x 0-based column
     * @param y 0-based row
     * @return this
     */
    public TerminalBuilder cursor(int x, int y) {
        cursorX = x;
        cursorY = y;
        return this;
    }

    /**
     * Sets the cursor X (column) only.
     *
     * @param x 0-based column
     * @return this
     */
    public TerminalBuilder x(int x) {
        cursorX = x;
        return this;
    }

    /**
     * Sets the cursor Y (row) only.
     *
     * @param y 0-based row
     * @return this
     */
    public TerminalBuilder y(int y) {
        cursorY = y;
        return this;
    }

    /**
     * Moves the cursor by a relative offset, wrapping horizontally.
     * Overflow in X increments Y; overflow in Y is unchecked.
     *
     * @param dx column delta
     * @param dy row delta
     */
    private void moveCursorWithWrap(int dx, int dy) {
        int totalX = cursorX + dx;
        cursorX = Math.floorMod(totalX, width);
        cursorY += Math.floorDiv(totalX, width) + dy;
    }

    /**
     * Moves the cursor by a relative offset without wrapping.
     *
     * @param x column delta
     * @param y row delta
     * @return this
     */
    public TerminalBuilder move(int x, int y) {
        cursorX += x;
        cursorY += y;
        return this;
    }

    // -------------------------------------------------------------------------
    // Writing
    // -------------------------------------------------------------------------

    /**
     * Appends a MiniMessage escape sequence at the given cell without writing
     * a visible character. Does <em>not</em> advance the cursor.
     *
     * <p>Use this to apply color/style to a specific cell before writing a char there.
     *
     * @param mini MiniMessage string (e.g. {@code "<red>"})
     * @param x    0-based column
     * @param y    0-based row
     * @return this
     */
    public TerminalBuilder mini(String mini, int x, int y) {
        escapes.get(y).get(x).append(ANSIUtil.stripNonAnsi(mm(mini, true)));
        return this;
    }

    /**
     * Appends a MiniMessage escape sequence at the current cursor position.
     * Does <em>not</em> advance the cursor.
     *
     * @param mini MiniMessage string
     * @return this
     * @see #mini(String, int, int)
     */
    public TerminalBuilder mini(String mini) {
        return mini(mini, cursorX, cursorY);
    }

    /**
     * Writes a single character at the given position and advances the cursor by 1
     * (with horizontal wrap).
     *
     * @param c character to write
     * @param x 0-based column
     * @param y 0-based row
     * @return this
     */
    public TerminalBuilder c(char c, int x, int y) {
        contents.get(y).set(x, c);
        return this; // no move
    }



    /**
     * Writes a single character at the current cursor position and advances the cursor.
     *
     * @param c character to write
     * @return this
     */
    public TerminalBuilder c(char c) {
        contents.get(cursorY).set(cursorX, c);
        moveCursorWithWrap(1, 0);
        return this;
    }
    /**
     * Writes a plain string starting at the given position and advances the cursor.
     * No MiniMessage parsing; characters are written verbatim.
     * Truncated at {@code width}.
     *
     * @param s string to write
     * @param x 0-based starting column
     * @param y 0-based row
     * @return this
     */
    public TerminalBuilder append(String s, int x, int y) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length && x + i < width; i++) {
            contents.get(y).set(x + i, chars[i]);
        }
        return this; // no move
    }

    /**
     * Writes a plain string at the current cursor position and advances the cursor.
     *
     * @param s string to write
     * @return this
     * @see #append(String, int, int)
     */
    public TerminalBuilder append(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length && cursorX + i < width; i++) {
            contents.get(cursorY).set(cursorX + i, chars[i]);
        }
        moveCursorWithWrap(s.length(), 0);
        return this;
    }

    /**
     * Writes a MiniMessage-formatted string starting at the given position,
     * splitting ANSI escapes and plain characters into separate buffers.
     * Advances the cursor by the plain-text length (with wrap).
     * Truncated at {@code width}.
     *
     * @param s MiniMessage string
     * @param x 0-based starting column
     * @param y 0-based row
     * @return this
     */
    public TerminalBuilder appendMini(String s, int x, int y) {
        s = mm(s, true);
        String plainText = AttributedString.stripAnsi(s);
        List<Map.Entry<Integer, String>> escapeCodes = ANSIUtil.extractAnsiCodes(s);

        int escIdx = 0;
        for (int i = 0; i < plainText.length(); i++) {
            int col = x + i;
            if (col >= width) break;

            while (escIdx < escapeCodes.size() && escapeCodes.get(escIdx).getKey() == i) {
                escapes.get(y).get(col).append(escapeCodes.get(escIdx).getValue());
                escIdx++;
            }

            contents.get(y).set(col, plainText.charAt(i));
        }


        return this;
    }

    /**
     * Writes a MiniMessage-formatted string at the current cursor position.
     *
     * @param s MiniMessage string
     * @return this
     * @see #appendMini(String, int, int)
     */
    public TerminalBuilder appendMini(String s) {
        s = mm(s, true);
        String plainText = AttributedString.stripAnsi(s);
        List<Map.Entry<Integer, String>> escapeCodes = ANSIUtil.extractAnsiCodes(s);

        int escIdx = 0;
        for (int i = 0; i < plainText.length(); i++) {
            int col = cursorX + i;
            if (col >= width) break;

            while (escIdx < escapeCodes.size() && escapeCodes.get(escIdx).getKey() == i) {
                escapes.get(cursorY).get(col).append(escapeCodes.get(escIdx).getValue());
                escIdx++;
            }

            contents.get(cursorY).set(col, plainText.charAt(i));
        }

        moveCursorWithWrap(plainText.length(), 0);
        return this;
    }
    /**
     * Writes a character repeatedly starting at the given position.
     * Advances the cursor by {@code times} (with wrap).
     * Truncated at {@code width}.
     *
     * @param c     character to repeat
     * @param times number of times to write
     * @param x     0-based starting column
     * @param y     0-based row
     * @return this
     */
    public TerminalBuilder repeat(char c, int times, int x, int y) {
        for (int i = 0; i < times && x + i < width; i++) {
            contents.get(y).set(x + i, c);
        }
        return this;
    }

    /**
     * Writes a character repeatedly at the current cursor position.
     *
     * @param c     character to repeat
     * @param times number of times to write
     * @return this
     * @see #repeat(char, int, int, int)
     */
    public TerminalBuilder repeat(char c, int times) {
        for (int i = 0; i < times && cursorX + i < width; i++) {
            contents.get(cursorY).set(cursorX + i, c);
        }
        moveCursorWithWrap(times, 0);
        return this;
    }

    // -------------------------------------------------------------------------
    // View composition
    // -------------------------------------------------------------------------


    /**
     * Renders a {@link View} into a sub-buffer of the given dimensions and composites
     * it into this builder at the specified position.
     *
     * <p>The view's {@link View#redraw(TerminalBuilder)} is called with a fresh
     * {@code TerminalBuilder} of size ({@code width} × {@code height}). The resulting
     * contents and escapes are merged cell-by-cell into this builder.
     * Cells that fall outside this builder's bounds are skipped.
     * Advances the cursor by {@code width} columns (with wrap).
     *
     * @param view   view to render
     * @param x      0-based target column
     * @param y      0-based target row
     * @param width  width of the sub-buffer
     * @param height height of the sub-buffer
     * @return this
     */
    public TerminalBuilder renderView(View view, int x, int y, int width, int height) {
        TerminalBuilder sub = new TerminalBuilder(width, height);
        view.redraw(sub);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int tx = x + col;
                int ty = y + row;
                if (ty >= this.height || tx >= this.width) continue;
                contents.get(ty).set(tx, sub.getContents().get(row).get(col));
                escapes.get(ty).get(tx).append(sub.getEscapes().get(row).get(col));
            }
        }
        return this;
    }

    /**
     * Renders a {@link View} into a sub-buffer of the given dimensions and composites
     * it into this builder at the current cursor position.
     * Advances the cursor to the end of the composited region.
     */
    public TerminalBuilder renderView(View view, int width, int height) {
        TerminalBuilder sub = new TerminalBuilder(width, height);
        view.redraw(sub);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int tx = cursorX + col;
                int ty = cursorY + row;
                if (ty >= this.height || tx >= this.width) continue;
                contents.get(ty).set(tx, sub.getContents().get(row).get(col));
                escapes.get(ty).get(tx).append(sub.getEscapes().get(row).get(col));
            }
        }

        cursorX = Math.floorMod(cursorX + width, this.width);
        cursorY += cursorY + height - 1 + Math.floorDiv(cursorX + width, this.width);
        return this;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /**
     * Serializes the buffer to an ANSI string positioned at ({@code x}, {@code y})
     * in the terminal.
     *
     * <p>Each cell is emitted as a {@code <cursor_set_pos>} MiniMessage tag followed
     * by any stored escape codes and the character. Coordinates are 1-based as
     * required by terminal escape sequences.
     *
     * @param x 1-based terminal column for the top-left corner
     * @param y 1-based terminal row for the top-left corner
     * @return ANSI escape string ready to be printed
     * @throws IllegalArgumentException if {@code x} or {@code y} is less than 1
     */
    public String render(int x, int y) {
        if (x < 0 || y < 0)
            throw logger.throwing(new IllegalArgumentException("Coordinates must be 0-based and positive"));

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < height; i++) {
            List<Character> row = contents.get(i);
            List<StringBuilder> escRow = escapes.get(i);

            for (int j = 0; j < width; j++) {
                sb.append(mm("<cursor_set_pos:" + (y + j + 1) + ":" + (i + x + 1) + ">"));
                sb.append(escRow.get(j)).append(row.get(j));
            }
        }

        return sb.toString();
    }

    /**
     * Serializes the buffer to an ANSI string positioned at (1, 1) — the top-left
     * of the terminal.
     *
     * @return ANSI escape string ready to be printed
     * @see #render(int, int)
     */
    public String render() {
        return render(1, 1);
    }
}