package dev.redgamer6427a.core.console.input;

import dev.redgamer6427a.core.logging.Logger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Full Kitty Keyboard Protocol implementation.
 *
 * <h2>Wire formats handled</h2>
 * <ol>
 *   <li>Full CSI u: {@code ESC [ cp[:shifted[:base]] ; mods:event:text1[:text2…] u}</li>
 *   <li>Functional with mods: {@code ESC [ 1 ; mods [ABCDEFHPQS~]}</li>
 *   <li>Functional no mods: {@code ESC [ [ABCDEFHPQS]}, {@code ESC O [PQRS]}</li>
 *   <li>Legacy bytes: {@code 0x0D} Enter, {@code 0x7F/0x08} Backspace, {@code 0x09} Tab,
 *       {@code 0x1B} lone ESC</li>
 *   <li>Mouse: {@code ESC [ < Pb ; Px ; Py M/m} (SGR 1006), {@code ESC [ M} (X10/normal)</li>
 * </ol>
 *
 * <h2>Progressive enhancement flags (send via {@code ESC [ > flags u})</h2>
 * <ul>
 *   <li>0x01 – Disambiguate escape codes</li>
 *   <li>0x02 – Report event types (press/repeat/release)</li>
 *   <li>0x04 – Report alternate keys (shifted + base-layout)</li>
 *   <li>0x08 – Report all keys as escape codes</li>
 *   <li>0x10 – Report associated text</li>
 * </ul>
 *
 * @see <a href="https://sw.kovidgoyal.net/kitty/keyboard-protocol/">Kitty Keyboard Protocol</a>
 */
public class KittyTerminalInput {

    // -------------------------------------------------------------------------
    // Static state
    // -------------------------------------------------------------------------

    private static final Map<UUID, KittyInputHook> inputHooks = new LinkedHashMap<>();
    private static final Logger logger = Logger.create(KittyTerminalInput.class);
    private static InputHook inputHook;

    // -------------------------------------------------------------------------
    // CSI u – full kitty sequence
    // Group 1 = codepoint  (mandatory)
    // Group 2 = shifted-key   (optional sub-param, after first ':')
    // Group 3 = base-layout   (optional sub-param, after second ':')
    // Group 4 = modifiers     (optional, after ';')
    // Group 5 = event-type    (optional sub-param of mods, after ':')
    // Group 6 = text codepoints, colon-separated (optional, after second ':' of the second field)
    // -------------------------------------------------------------------------
    private static final Pattern CSI_U = Pattern.compile(
            "\033\\[" +
                    "([0-9]+)" +                             // cp
                    "(?::([0-9]*)(?::([0-9]*))?)?" +         // [:shifted[:base]]
                    "(?:;([0-9]+)" +                         // ;mods
                    "(?::([0-9]*)" +                         // :event-type
                    "(?::([0-9:]*))?" +                      // :text-codepoints (colon-sep)
                    ")?)?" +
                    "u"
    );

    // CSI number ; mods [~ABCDEFHPQS]  — legacy functional keys with modifiers
    private static final Pattern CSI_FUNC_MOD = Pattern.compile(
            "\033\\[([0-9]+);([0-9]+)([~ABCDEFHPQS])"
    );

    // CSI 1 ; mods [ABCDEFHPQS]  — cursor/function keys with mods, number is 1
    private static final Pattern CSI_1_MOD = Pattern.compile(
            "\033\\[1;([0-9]+)([ABCDEFHPQS])"
    );

    // CSI [ABCDEFHPQS]  — no modifiers
    private static final Pattern CSI_SIMPLE = Pattern.compile(
            "\033\\[([ABCDEFHPQS])"
    );

    // SS3 [PQRS]  — F1-F4 (ESC O letter)
    private static final Pattern SS3 = Pattern.compile(
            "\033O([PQRS])"
    );

    // SGR mouse: ESC [ < Pb ; Px ; Py M/m
    private static final Pattern MOUSE_SGR = Pattern.compile(
            "\033\\[<([0-9]+);([0-9]+);([0-9]+)([Mm])"
    );

    // X10/normal mouse: ESC [ M + 3 raw bytes
    // Handled specially in streaming parser (fixed-length payload).

    // -------------------------------------------------------------------------
    // Listener bootstrap
    // -------------------------------------------------------------------------

    private static void startListener() {
        inputHook = RawTerminalInput.makeHook();
        // The buffer accumulates bytes between ESC and a terminator.
        AtomicReference<String> buf = new AtomicReference<>("");

        inputHook.executor(character -> {
            try {
                processCharacter(character, buf);
            } catch (Exception e) {
                logger.catching(e);
            }
        });
    }

    /**
     * Core streaming parser.  All escape sequences start with ESC (0x1B) and are
     * accumulated in {@code buf} until a recognised terminator is found.
     */
    private static void processCharacter(char ch, AtomicReference<String> buf) {
        String current = buf.get();

        // ---------- Non-escape bytes while no buffer active ----------
        if (current.isEmpty() && ch != '\033') {
            handleLegacyByte(ch);
            return;
        }

        // ---------- Start of an escape sequence ----------
        if (ch == '\033') {
            if (!current.isEmpty()) {
                // Flush previous incomplete sequence as bare ESC, restart.
                broadcast(KeyEvent.ofSpecial(SpecialKey.ESCAPE, 1));
            }
            buf.set("\033");
            return;
        }

        // ---------- Accumulate ----------
        String next = current + ch;
        buf.set(next);

        // ---------- X10 mouse: ESC [ M + 3 bytes ----------
        if (next.equals("\033[M")) {
            // Next 3 bytes are the mouse payload — keep accumulating.
            return;
        }
        if (next.startsWith("\033[M") && next.length() == 6) {
            handleX10Mouse(next);
            buf.set("");
            return;
        }

        // ---------- SS3 (ESC O x) — two-char sequences after ESC ----------
        if (next.startsWith("\033O") && next.length() == 3) {
            Matcher m = SS3.matcher(next);
            if (m.matches()) {
                SpecialKey key = ss3Letter(next.charAt(2));
                broadcast(KeyEvent.ofSpecial(key, 1));
            } else {
                // Unknown SS3 — emit bare ESC + remainder
                broadcast(KeyEvent.ofSpecial(SpecialKey.ESCAPE, 1));
                handleLegacyByte(next.charAt(1));
                handleLegacyByte(next.charAt(2));
            }
            buf.set("");
            return;
        }

        // ---------- CSI sequences (ESC [ …) ----------
        if (!next.startsWith("\033[")) {
            // Two-char ESC sequences that aren't CSI or SS3 → alt+key
            if (next.length() == 2) {
                int altMod = Modifier.ALT.bit + 1; // mods value for Alt
                broadcast(new KeyEvent(next.charAt(1), altMod, EventType.PRESS, SpecialKey.NONE, 0, 0, null));
                buf.set("");
            }
            return;
        }

        // SGR mouse terminates with M or m
        if (next.startsWith("\033[<") && (ch == 'M' || ch == 'm')) {
            Matcher m = MOUSE_SGR.matcher(next);
            if (m.matches()) {
                handleSgrMouse(m);
            }
            buf.set("");
            return;
        }

        // Kitty CSI u terminates with 'u'
        if (ch == 'u') {
            // Could be CSI u (kitty) or CSI < n u (pop stack — control sequence, not a key)
            if (next.startsWith("\033[<") || next.startsWith("\033[>") || next.startsWith("\033[=")) {
                // Mode-change control sequences sent TO the terminal — not key events.
                // If we somehow receive these (e.g. echo), discard.
                buf.set("");
                return;
            }
            if (next.equals("\033[u")) {
                // Query response: terminal reports current flags — not a key event.
                buf.set("");
                return;
            }
            Matcher m = CSI_U.matcher(next);
            if (m.matches()) {
                broadcast(parseCsiU(m));
                buf.set("");
                return;
            }
        }

        // Legacy functional keys: terminate with [~ABCDEFHPQS]
        if (isLegacyTerminator(ch)) {
            // CSI 1 ; mods [ABCDEFHPQS]
            Matcher m1 = CSI_1_MOD.matcher(next);
            if (m1.matches()) {
                broadcast(parseCsi1Mod(m1));
                buf.set("");
                return;
            }
            // CSI number ; mods [~ABCDEFHPQS]
            Matcher m2 = CSI_FUNC_MOD.matcher(next);
            if (m2.matches()) {
                broadcast(parseCsiFuncMod(m2));
                buf.set("");
                return;
            }
            // CSI [ABCDEFHPQS]  (no mods)
            Matcher m3 = CSI_SIMPLE.matcher(next);
            if (m3.matches()) {
                SpecialKey key = csiLetter(next.charAt(2), -1);
                broadcast(KeyEvent.ofSpecial(key, 1));
                buf.set("");
                return;
            }
            // Unrecognised — discard.
            buf.set("");
        }

        // If buffer is suspiciously long without terminating → flush.
        if (next.length() > 64) {
            logger.warning("KittyTerminalInput: escape buffer overflow, flushing: {}", escape(next));
            buf.set("");
        }
    }

    // -------------------------------------------------------------------------
    // Parsers
    // -------------------------------------------------------------------------

    /** Parse a full CSI u kitty sequence. */
    private static KeyEvent parseCsiU(Matcher m) {
        int cp        = Integer.parseInt(m.group(1));
        int shifted   = parseOptionalInt(m.group(2), 0);
        int baseLayout= parseOptionalInt(m.group(3), 0);
        int mods      = parseOptionalInt(m.group(4), 1);
        int evType    = parseOptionalInt(m.group(5), 1);
        String textField = m.group(6); // may be null

        int[] textCodepoints = null;
        if (textField != null && !textField.isEmpty()) {
            String[] parts = textField.split(":");
            textCodepoints = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                textCodepoints[i] = parseOptionalInt(parts[i], 0);
            }
        }

        SpecialKey special = SpecialKey.fromCodepoint(cp);
        EventType event = EventType.fromInt(evType);
        return new KeyEvent(cp, mods, event, special, shifted, baseLayout, textCodepoints);
    }

    /**
     * CSI 1 ; mods LETTER — cursor/function key with modifiers.
     * Terminal sends this for arrows, Home, End, F1-F4, PgUp/Dn.
     */
    private static KeyEvent parseCsi1Mod(Matcher m) {
        int mods = Integer.parseInt(m.group(1));
        char letter = m.group(2).charAt(0);
        SpecialKey key = csiLetter(letter, -1);
        return KeyEvent.ofSpecial(key, mods);
    }

    /**
     * CSI number ; mods TERMINATOR — numbered functional key with modifiers.
     * e.g. CSI 5 ; 2 ~ → Shift+PageUp
     */
    private static KeyEvent parseCsiFuncMod(Matcher m) {
        int number = Integer.parseInt(m.group(1));
        int mods   = Integer.parseInt(m.group(2));
        char term  = m.group(3).charAt(0);
        SpecialKey key = (term == '~') ? tiNumberedKey(number) : csiLetter(term, number);
        return KeyEvent.ofSpecial(key, mods);
    }

    // -------------------------------------------------------------------------
    // Legacy byte handling
    // -------------------------------------------------------------------------

    private static void handleLegacyByte(char ch) {
        switch (ch) {
            case '\r', '\n' -> broadcast(KeyEvent.ofSpecial(SpecialKey.ENTER, 1));
            case '\t'       -> broadcast(KeyEvent.ofSpecial(SpecialKey.TAB, 1));
            case 0x7F, 0x08 -> broadcast(KeyEvent.ofSpecial(SpecialKey.BACKSPACE, 1));
            case 0x1B       -> broadcast(KeyEvent.ofSpecial(SpecialKey.ESCAPE, 1));
            case ' '        -> broadcast(KeyEvent.ofSpecial(SpecialKey.SPACE, 1));
            default -> {
                if (ch < 0x20) {
                    // C0 control: ctrl+letter (e.g. 0x01=ctrl+a, 0x03=ctrl+c)
                    int letter = ch + 0x60; // 0x01→'a', 0x03→'c' …
                    int ctrlMod = 1 + Modifier.CTRL.bit;
                    broadcast(new KeyEvent(letter, ctrlMod, EventType.PRESS, SpecialKey.NONE, 0, 0, null));
                } else {
                    // Plain printable character
                    broadcast(new KeyEvent(ch, 1, EventType.PRESS, SpecialKey.NONE, 0, 0, null));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mouse handling
    // -------------------------------------------------------------------------

    /** X10/Normal mouse report: ESC [ M + 3 raw bytes. */
    private static void handleX10Mouse(String seq) {
        if (seq.length() != 6) {
            logger.warning("Invalid X10 mouse sequence length: {}", String.valueOf(seq.length()));
            return;
        }

        int btnByte = seq.charAt(3) - 32;
        int col     = seq.charAt(4) - 32;
        int row     = seq.charAt(5) - 32;

      //  logger.finest("X10 mouse: btn={} col={} row={}", String.valueOf(btnByte), String.valueOf(col), String.valueOf(row));
        broadcast(MouseEvent.of(btnByte, col, row, false));
    }

    /** SGR mouse: ESC [ < Pb ; Px ; Py M/m */
    private static void handleSgrMouse(Matcher m) {
        int btnByte = Integer.parseInt(m.group(1));
        int col     = Integer.parseInt(m.group(2));
        int row     = Integer.parseInt(m.group(3));
        boolean release = m.group(4).equals("m");
        broadcast(MouseEvent.of(btnByte, col, row, release));
    }

    // -------------------------------------------------------------------------
    // Key lookup helpers
    // -------------------------------------------------------------------------

    /** Map a CSI final letter to a SpecialKey (when no tilde, number context). */
    private static SpecialKey csiLetter(char letter, int number) {
        return switch (letter) {
            case 'A' -> SpecialKey.ARROW_UP;
            case 'B' -> SpecialKey.ARROW_DOWN;
            case 'C' -> SpecialKey.ARROW_RIGHT;
            case 'D' -> SpecialKey.ARROW_LEFT;
            case 'E' -> SpecialKey.KP_BEGIN;
            case 'F' -> SpecialKey.END;
            case 'H' -> SpecialKey.HOME;
            case 'P' -> SpecialKey.F1;
            case 'Q' -> SpecialKey.F2;
            case 'S' -> SpecialKey.F4;
            case '~' -> tiNumberedKey(number);
            default  -> SpecialKey.UNKNOWN;
        };
    }

    /** SS3 letter (F1–F4). */
    private static SpecialKey ss3Letter(char ch) {
        return switch (ch) {
            case 'P' -> SpecialKey.F1;
            case 'Q' -> SpecialKey.F2;
            case 'R' -> SpecialKey.F3;
            case 'S' -> SpecialKey.F4;
            default  -> SpecialKey.UNKNOWN;
        };
    }

    /** Map terminfo CSI ~ numbers to SpecialKey. */
    private static SpecialKey tiNumberedKey(int n) {
        return switch (n) {
            case 1  -> SpecialKey.HOME;
            case 2  -> SpecialKey.INSERT;
            case 3  -> SpecialKey.DELETE;
            case 4  -> SpecialKey.END;
            case 5  -> SpecialKey.PAGE_UP;
            case 6  -> SpecialKey.PAGE_DOWN;
            case 7  -> SpecialKey.HOME;   // alt encoding
            case 8  -> SpecialKey.END;    // alt encoding
            case 11 -> SpecialKey.F1;
            case 12 -> SpecialKey.F2;
            case 13 -> SpecialKey.F3;
            case 14 -> SpecialKey.F4;
            case 15 -> SpecialKey.F5;
            case 17 -> SpecialKey.F6;
            case 18 -> SpecialKey.F7;
            case 19 -> SpecialKey.F8;
            case 20 -> SpecialKey.F9;
            case 21 -> SpecialKey.F10;
            case 23 -> SpecialKey.F11;
            case 24 -> SpecialKey.F12;
            case 25 -> SpecialKey.F13;
            case 26 -> SpecialKey.F14;
            case 28 -> SpecialKey.F15;
            case 29 -> SpecialKey.F16;
            case 31 -> SpecialKey.F17;
            case 32 -> SpecialKey.F18;
            case 33 -> SpecialKey.F19;
            case 34 -> SpecialKey.F20;
            case 200 -> SpecialKey.BRACKETED_PASTE_START;
            case 201 -> SpecialKey.BRACKETED_PASTE_END;
            default -> SpecialKey.UNKNOWN;
        };
    }

    private static boolean isLegacyTerminator(char ch) {
        return "~ABCDEFHPQSu".indexOf(ch) >= 0;
    }

    private static int parseOptionalInt(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    /** Pretty-print non-printable chars for logging. */
    private static String escape(String s) {
        return s.replace("\033", "<ESC>").replace("\r", "<CR>").replace("\n", "<LF>");
    }

    // -------------------------------------------------------------------------
    // Broadcast
    // -------------------------------------------------------------------------

    private static void broadcast(Object event) {
        for (KittyInputHook hook : inputHooks.values()) {
            if (event instanceof KeyEvent ke)   hook.onKey(ke);
            if (event instanceof MouseEvent me) hook.onMouse(me);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------


    public static KittyInputHook makeHook() {
        UUID uuid = UUID.randomUUID();
        KittyInputHook hook = new KittyInputHook(() -> stopInput(uuid));
        inputHooks.put(uuid, hook);
        if (inputHooks.size() == 1) {
            startListener();
            RawTerminalInput.startKitty();
        }
        return hook;
    }

    private static void stopInput(UUID uuid) {
        inputHooks.remove(uuid);
        if (inputHooks.isEmpty()) {
            inputHook.close();
            RawTerminalInput.stopKitty();
            inputHook = null;
        }
    }

    public static void closeAll() {
        new ArrayList<>(inputHooks.values()).forEach(KittyInputHook::close);
    }

    /**
     * Send a progressive-enhancement enable sequence to stdout.
     * Call this after makeHook() to enable the desired feature flags.
     *
     * @param flags  bitmask of {@link EnhancementFlag} values
     */
    public static void enableEnhancements(int flags) {
        // CSI > flags u  (push flags onto terminal's mode stack)
        System.out.printf("\033[>%du", flags);
        System.out.flush();
    }

    /**
     * Pop the top level of progressive enhancement from the terminal's stack.
     * Call this on cleanup / before exit.
     */
    public static void disableEnhancements() {
        // CSI < u
        System.out.print("\033[<u");
        System.out.flush();
    }

    /**
     * Query the terminal for its current enhancement flags.
     * The terminal responds with {@code CSI ? flags u}.
     */
    public static void queryEnhancements() {
        System.out.print("\033[?u");
        System.out.flush();
    }

    // =========================================================================
    // Data types
    // =========================================================================

    /**
     * A keyboard event.
     *
     * <h3>Field semantics</h3>
     * <ul>
     *   <li>{@code codepoint} – Unicode codepoint of the key (or PUA for functional keys).</li>
     *   <li>{@code modifiers} – kitty mods value ({@code 1 + bitmask}).  Use {@link Modifier} helpers.</li>
     *   <li>{@code eventType} – {@link EventType}.</li>
     *   <li>{@code specialKey} – resolved {@link SpecialKey}, or {@link SpecialKey#NONE} for regular chars.</li>
     *   <li>{@code shiftedKey} – codepoint of the shifted variant (0 = absent), e.g. Shift+3 → '#'.</li>
     *   <li>{@code baseLayoutKey} – codepoint of the base-layout key (0 = absent), useful for non-Latin layouts.</li>
     *   <li>{@code textCodepoints} – associated text codepoints when "Report associated text" is enabled; may be null.</li>
     * </ul>
     */
    public record KeyEvent(
            int codepoint,
            int modifiers,
            EventType eventType,
            SpecialKey specialKey,
            int shiftedKey,
            int baseLayoutKey,
            int[] textCodepoints
    ) {
        // Convenience factory for special keys from legacy encoding.
        static KeyEvent ofSpecial(SpecialKey key, int mods) {
            return new KeyEvent(key.codepoint(), mods, EventType.PRESS, key, 0, 0, null);
        }

        // ---- Event type predicates ----
        public boolean isPress()   { return eventType == EventType.PRESS; }
        public boolean isRepeat()  { return eventType == EventType.REPEAT; }
        public boolean isRelease() { return eventType == EventType.RELEASE; }

        // ---- Modifier predicates ----
        private int modBits() { return modifiers - 1; }
        public boolean shift()    { return (modBits() & Modifier.SHIFT.bit)     != 0; }
        public boolean alt()      { return (modBits() & Modifier.ALT.bit)       != 0; }
        public boolean ctrl()     { return (modBits() & Modifier.CTRL.bit)      != 0; }
        public boolean superKey() { return (modBits() & Modifier.SUPER.bit)     != 0; }
        public boolean hyper()    { return (modBits() & Modifier.HYPER.bit)     != 0; }
        public boolean meta()     { return (modBits() & Modifier.META.bit)      != 0; }
        public boolean capsLock() { return (modBits() & Modifier.CAPS_LOCK.bit) != 0; }
        public boolean numLock()  { return (modBits() & Modifier.NUM_LOCK.bit)  != 0; }

        /** The character this key produces (if it is a printable Unicode codepoint). */
        public char character() { return (char) codepoint; }

        /** True if this is a printable character (not a functional/special key). */
        public boolean isPrintable() { return specialKey == SpecialKey.NONE && codepoint >= 0x20; }

        /** The text string associated with this event (from text-reporting mode), or null. */
        public String text() {
            if (textCodepoints == null) return null;
            StringBuilder sb = new StringBuilder();
            for (int cp : textCodepoints) sb.appendCodePoint(cp);
            return sb.toString();
        }

        @Override public @NotNull String toString() {
            return "KeyEvent{cp=%d(%s) mods=%d ev=%s special=%s shifted=%d base=%d text=%s}"
                    .formatted(codepoint, isPrintable() ? String.valueOf(character()) : "?",
                            modifiers, eventType, specialKey, shiftedKey, baseLayoutKey, text());
        }
    }

    /** A mouse event decoded from SGR (1006) or X10 mouse encoding. */
    public record MouseEvent(int button, int col, int row, boolean release, boolean motion,
                             boolean shift, boolean alt, boolean ctrl) {
        /**
         * Decode a raw button byte (as used by both X10 and SGR encodings).
         * In X10 the button byte has 32 added; SGR sends the raw Pb value.
         */
        static MouseEvent of(int pb, int col, int row, boolean release) {
            // Bit layout of Pb:
            // bits 0-1 : button number (0=left,1=middle,2=right,3=release/none)
            // bit  2   : shift
            // bit  3   : meta/alt
            // bit  4   : ctrl
            // bit  5   : motion event
            // bit  6   : extra button offset (buttons 4-7)
            // bit  7   : extra button offset (buttons 8-11)
            int btn    = (pb & 0x03) | ((pb & 0x40) >> 4) | ((pb & 0x80) >> 4);
            boolean motion = (pb & 0x20) != 0;
            boolean sh = (pb & 0x04) != 0;
            boolean al = (pb & 0x08) != 0;
            boolean ct = (pb & 0x10) != 0;
            return new MouseEvent(btn, col, row, release, motion, sh, al, ct);
        }
    }

    // =========================================================================
    // Enums
    // =========================================================================

    /** Progressive enhancement flags to OR together and pass to {@link #enableEnhancements(int)}. */
    public enum EnhancementFlag {
        /** Fix ambiguities: Esc, ctrl+key, alt+key, app-keypad get CSI u. */
        DISAMBIGUATE(0x01),
        /** Emit press (1), repeat (2), and release (3) events. */
        REPORT_EVENT_TYPES(0x02),
        /** Include shifted-key and base-layout-key sub-parameters. */
        REPORT_ALTERNATE_KEYS(0x04),
        /** Report ALL keys (including printable) as escape codes. */
        REPORT_ALL_KEYS(0x08),
        /** Append associated text codepoints after event-type sub-parameter. */
        REPORT_ASSOCIATED_TEXT(0x10);

        public final int bit;
        EnhancementFlag(int bit) { this.bit = bit; }

        /** Convenience: OR of all flags for maximum protocol richness. */
        public static final int ALL = 0x1F;
    }

    /** Modifier keys and their bit positions within the kitty modifier bitmask. */
    public enum Modifier {
        SHIFT(1),
        ALT(2),
        CTRL(4),
        SUPER(8),
        HYPER(16),
        META(32),
        CAPS_LOCK(64),
        NUM_LOCK(128);

        public final int bit;
        Modifier(int bit) { this.bit = bit; }
    }

    /** Key event types. */
    public enum EventType {
        PRESS(1), REPEAT(2), RELEASE(3);
        public final int value;
        EventType(int v) { this.value = v; }
        public static EventType fromInt(int v) {
            return switch (v) { case 2 -> REPEAT; case 3 -> RELEASE; default -> PRESS; };
        }
    }

    /**
     * All special / functional keys, including the full Private Use Area table
     * defined by the Kitty keyboard protocol (codepoints 57344–63743).
     *
     * <p>Ordinary Unicode characters use {@link #NONE}.  PUA assignments follow
     * the official kitty functional key table.</p>
     */
    public enum SpecialKey {
        // Sentinel
        NONE(-1),
        UNKNOWN(-2),

        // ---- C0 control / legacy ----
        ENTER(0x0D),
        TAB(0x09),
        BACKSPACE(0x7F),
        ESCAPE(0x1B),
        SPACE(0x20),
        DELETE(0x7F),   // also used via CSI 3 ~

        // ---- Kitty PUA functional keys (57344 = 0xE000) ----
        // Navigation
        ARROW_UP(57352),
        ARROW_DOWN(57353),
        ARROW_LEFT(57354),
        ARROW_RIGHT(57355),
        PAGE_UP(57356),
        PAGE_DOWN(57357),
        HOME(57358),
        END(57359),
        INSERT(57360),
        KP_BEGIN(57427),  // Keypad 5 (begin / KP_BEGIN)

        // Function keys
        F1(57344),
        F2(57345),
        F3(57346),
        F4(57347),
        F5(57348),
        F6(57349),
        F7(57350),
        F8(57351),
        F9(57428),
        F10(57429),
        F11(57430),
        F12(57431),
        F13(57432),
        F14(57433),
        F15(57434),
        F16(57435),
        F17(57436),
        F18(57437),
        F19(57438),
        F20(57439),
        F21(57440),
        F22(57441),
        F23(57442),
        F24(57443),
        F25(57444),
        F26(57445),
        F27(57446),
        F28(57447),
        F29(57448),
        F30(57449),
        F31(57450),
        F32(57451),
        F33(57452),
        F34(57453),
        F35(57454),

        // Modifier-only keys
        LEFT_SHIFT(57441),
        LEFT_CTRL(57442),
        LEFT_ALT(57443),
        LEFT_SUPER(57444),
        LEFT_HYPER(57445),
        LEFT_META(57446),
        RIGHT_SHIFT(57447),
        RIGHT_CTRL(57448),
        RIGHT_ALT(57449),
        RIGHT_SUPER(57450),
        RIGHT_HYPER(57451),
        RIGHT_META(57452),
        ISO_LEVEL3_SHIFT(57453),  // AltGr
        ISO_LEVEL5_SHIFT(57454),

        // Lock keys (only visible with REPORT_ALL_KEYS)
        CAPS_LOCK(57358 + 16),
        SCROLL_LOCK(57358 + 17),
        NUM_LOCK(57358 + 18),

        // Keypad (when NumLock is off or in app-keypad mode)
        KP_DECIMAL(57363),
        KP_DIVIDE(57364),
        KP_MULTIPLY(57365),
        KP_SUBTRACT(57366),
        KP_ADD(57367),
        KP_ENTER(57368),
        KP_EQUAL(57369),
        KP_SEPARATOR(57370),
        KP_LEFT(57371),
        KP_RIGHT(57372),
        KP_UP(57373),
        KP_DOWN(57374),
        KP_PAGE_UP(57375),
        KP_PAGE_DOWN(57376),
        KP_HOME(57377),
        KP_END(57378),
        KP_INSERT(57379),
        KP_DELETE(57380),
        KP_0(57399),
        KP_1(57400),
        KP_2(57401),
        KP_3(57402),
        KP_4(57403),
        KP_5(57404),
        KP_6(57405),
        KP_7(57406),
        KP_8(57407),
        KP_9(57408),

        // Media keys
        MEDIA_PLAY(57457),
        MEDIA_PAUSE(57458),
        MEDIA_PLAY_PAUSE(57459),
        MEDIA_REVERSE(57460),
        MEDIA_STOP(57461),
        MEDIA_FAST_FORWARD(57462),
        MEDIA_REWIND(57463),
        MEDIA_TRACK_NEXT(57464),
        MEDIA_TRACK_PREV(57465),
        MEDIA_RECORD(57466),
        LOWER_VOLUME(57467),
        RAISE_VOLUME(57468),
        MUTE_VOLUME(57469),

        // Misc
        PRINT_SCREEN(57361),
        PAUSE(57362),
        MENU(57363 + 98),

        // Bracketed paste markers (CSI 200 ~ / CSI 201 ~)
        BRACKETED_PASTE_START(57535),
        BRACKETED_PASTE_END(57536),
        ;

        private int cp;
        SpecialKey(int cp) { this.cp = cp; }

        /** The codepoint associated with this key (PUA or legacy). */
        public int codepoint() { return cp; }

        // Reverse lookup: codepoint → SpecialKey
        private static final Map<Integer, SpecialKey> BY_CODEPOINT = new HashMap<>();
        static {
            for (SpecialKey k : values()) {
                if (k.cp > 0) BY_CODEPOINT.put(k.cp, k);
            }
        }

        public static SpecialKey fromCodepoint(int cp) {
            return BY_CODEPOINT.getOrDefault(cp, NONE);
        }

        @Getter
        private final List<Integer> codes = List.of(cp);
    }
}