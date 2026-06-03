package dev.redgamer6427a.core.console.output;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum ANSICode {
    // Bell
    BELL("bell", 0, "\007"),

    // Cursor movement
    CURSOR_UP("cursor_up", 1, "\033[%sA", "1"),
    CURSOR_DOWN("cursor_down", 1, "\033[%sB", "1"),
    CURSOR_RIGHT("cursor_right", 1, "\033[%sC", "1"),
    CURSOR_LEFT("cursor_left", 1, "\033[%sD", "1"),
    CURSOR_NEXT_LINE("cursor_next_line", 1, "\033[%sE", "1"),
    CURSOR_PREVIOUS_LINE("cursor_previous_line", 1, "\033[%sF", "1"),
    CURSOR_SET_COL("cursor_set_col", 1, "\033[%sG", "1"),
    CURSOR_SET_POS("cursor_set_pos", 2, "\033[%s;%sH", "1", "1"),
    CURSOR_GET_POS("cursor_get_pos", 0, "\033[6n"),
    SCROLL_UP("scroll_up", 1, "\033[%sS", "1"),
    SCROLL_DOWN("scroll_down", 1, "\033[%sT", "1"),

    // Cursor visibility
    CURSOR_SHOW("cursor_show", 0, "\033[?25h"),
    CURSOR_HIDE("cursor_hide", 0, "\033[?25l"),
    CURSOR_BLOCK_BLINK("cursor_block_blink", 0, "\033[1 q"),
    CURSOR_BLOCK("cursor_block", 0, "\033[2 q"),
    CURSOR_UNDERLINE_BLINK("cursor_underline_blink", 0, "\033[3 q"),
    CURSOR_UNDERLINE("cursor_underline", 0, "\033[4 q"),
    CURSOR_BEAM_BLINK("cursor_beam_blink", 0, "\033[5 q"),
    CURSOR_BEAM("cursor_beam", 0, "\033[6 q"),
    CURSOR_DEFAULT("cursor_default", 0, "\033[0 q"),

    // Clear
    CLEAR_SCREEN("clear_screen", 0, "\033[2J"),
    CLEAR_SCREEN_TO_END("clear_screen_to_end", 0, "\033[0J"),
    CLEAR_SCREEN_TO_START("clear_screen_to_start", 0, "\033[1J"),
    CLEAR_SCROLLBACK("clear_scrollback", 0, "\033[3J"),
    CLEAR_LINE("clear_line", 0, "\033[2K"),
    CLEAR_LINE_TO_END("clear_line_to_end", 0, "\033[0K"),
    CLEAR_LINE_TO_START("clear_line_to_start", 0, "\033[1K"),

    // Alternate
    ALTERNATE_BUFFER("alternate_buffer", 0, "\033[?1049h"),
    MAIN_BUFFER("main_buffer", 0, "\033[?1049l"),

    // Focus events
    FOCUS_ENABLE("focus_enable", 0, "\033[?1004h"),
    FOCUS_DISABLE("focus_disable", 0, "\033[?1004l"),

    // Bracketed paste
    BRACKETED_PASTE_ENABLE("bracketed_paste_enable", 0, "\033[?2004h"),
    BRACKETED_PASTE_DISABLE("bracketed_paste_disable", 0, "\033[?2004l"),

    // SGR - Reset
    RESET("reset", 0, "\033[0m"),
    RESET_S("r", 0, "\033[0m"),

    // SGR - Intensity
    BOLD("bold", 0, "\033[1m"),
    BOLD_S("b", 0, "\033[1m"),
    DIM("dim", 0, "\033[2m"),
    INTENSITY_NORMAL("/b", 0, "\033[22m"),
    INTENSITY_NORMAL_2("/d", 0, "\033[22m"),

    // SGR - Italic
    ITALIC("italic", 0, "\033[3m"),
    ITALIC_S("i", 0, "\033[3m"),
    ITALIC_OFF("/italic", 0, "\033[23m"),

    // SGR - Underline
    UNDERLINE("underline", 0, "\033[4m"),
    UNDERLINE_S("u", 0, "\033[4m"),
    UNDERLINE_DOUBLE("underline_double", 0, "\033[21m"),
    UNDERLINE_OFF("/underline", 0, "\033[24m"),
    // Kitty
    UNDERLINE_STRAIGHT("underline_straight", 0, "\033[4:1m"),
    UNDERLINE_DOUBLE_KIT("underline_double_kitty", 0, "\033[4:2m"),
    UNDERLINE_CURLY("underline_curly", 0, "\033[4:3m"),
    UNDERLINE_DOTTED("underline_dotted", 0, "\033[4:4m"),
    UNDERLINE_DASHED("underline_dashed", 0, "\033[4:5m"),
    UNDERLINE_STYLE_OFF("underline_style_off", 0, "\033[4:0m"),

    // SGR - Blink
    BLINK_SLOW("blink_slow", 0, "\033[5m"),
    BLINK_FAST("blink_fast", 0, "\033[6m"),
    BLINK_OFF("/blink", 0, "\033[25m"),

    // SGR - Misc (togglable)
    INVERT("invert", 0, "\033[7m"),
    INVERT_OFF("/invert", 0, "\033[27m"),
    HIDE("hide", 0, "\033[8m"),
    HIDE_OFF("/hide", 0, "\033[28m"),
    STRIKETHROUGH("strikethrough", 0, "\033[9m"),
    STRIKETHROUGH_OFF("/strikethrough", 0, "\033[29m"),

    // SGR - Foreground color
    FG_DEFAULT("fg_default", 0, "\033[39m"),
    FG_RGB("fg_rgb", 3, "\033[38;2;%s;%s;%sm", "255", "0", "0"),

    // SGR - Background color
    BG_DEFAULT("bg_default", 0, "\033[49m"),
    BG_RGB("bg_rgb", 3, "\033[48;2;%s;%s;%sm", "255", "0", "0"),

    // SGR - Underline color
    UNDERLINE_COLOR_RGB("underline_color_rgb", 3, "\033[58;2;%s;%s;%sm", "255", "0", "0"),
    UNDERLINE_COLOR_DEFAULT("underline_color_default", 0, "\033[59m"),


    ;

    private final String id;
    private final int paramCount;
    private final String representation;
    private final String[] defaults;

    ANSICode(String id, int paramCount, String representation, String... defaults) {
        this.id = id;
        this.paramCount = paramCount;
        this.representation = representation;
        this.defaults = defaults;
    }

    public String format(Object... args) {
        if (args.length > paramCount)
            throw new IllegalArgumentException("Expected " + paramCount + " args, got " + args.length);
        else if (args.length < paramCount) {

            List<String> modArgs = new ArrayList<>(Arrays.stream(args).map(Object::toString).toList());

            while (modArgs.size() < paramCount) {
                modArgs.add(defaults[modArgs.size()]);
            }
            return String.format(representation, modArgs.toArray());
        } else {
            return paramCount == 0 ? representation : String.format(representation, args);
        }
    }
}