package dev.redgamer6427a.core.console.input;

import dev.redgamer6427a.core.logging.Logger;
import lombok.Getter;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A very simple helper class for terminal input
 */
public class TerminalInput {

    private static boolean isRawMode = false;
    private static Thread inputThread;
    private static NonBlockingReader reader;

    private static final Logger logger = Logger.create();
    @Getter
    private static Terminal terminal;
    private static Attributes nonRawAttributes;
    private static PrintWriter out;

    /**
     * Starts raw mode.
     */
    public static void startRaw() {
        if (isRawMode) return;

        try {
            terminal = TerminalBuilder.builder().system(true).build();
        } catch (Exception e) {
            logger.catching(e);

        }
        out = terminal.writer();
        nonRawAttributes = terminal.getAttributes();
        terminal.enterRawMode();
        reader = terminal.reader();



        isRawMode = true;

        Runtime.getRuntime().addShutdownHook(new Thread(TerminalInput::stopRaw));
    }

    /**
     * Starts the kitty input mode.
     * @see KittyTerminalInput
     */
    public static void startKitty() {
        try {
            new ProcessBuilder("stty", "-icanon", "-echo")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Enable mouse reporting (SGR 1006 mode)
        out.write("\033[?1000h");
        out.flush();

        // Enable Kitty keyboard protocol (progressive enhancement)
        out.write("\033[>0u");  // Request keyboard enhancement support
        out.write("\033[=3;1u");
        out.flush();
    }

    /**
     * Stops the kitty input mode.
     * @see KittyTerminalInput
     */
    public static void stopKitty() {
        // Disable mouse reporting
        out.write("\033[?1000l");

        // Disable Kitty keyboard protocol
        out.write("\033[<u");

        out.flush();
    }

    /**
     * Exits raw mode.
     */
    public static void stopRaw() {
        if (!isRawMode) return;
        stopKitty();
        terminal.setAttributes(nonRawAttributes);
        isRawMode = false;
        
        try {
            terminal.close();
        } catch (IOException e) {
            logger.catching(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * A list of all InputHooks (not KittyInputHooks).
     */
    private static final Map<UUID, InputHook> inputHooks = new LinkedHashMap<>();

    /**
     * Creates and registers an InputHook
     * @return the InputHook
     */
    public static InputHook makeHook() {

        UUID uuid = UUID.randomUUID();
        InputHook hook = new InputHook(() -> stopInput(uuid));

        inputHooks.put(uuid, hook);

        if (inputHooks.size() == 1) {
            startRaw();
            inputThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        int ch = reader.read();

                        if (ch == -1) break;


                        new ArrayList<>(inputHooks.values()).forEach(h -> h.call((char) ch));
                    }
                } catch (IOException e) {
                    logger.catching(e);


                }
            }, "jtui-input");
            inputThread.setDaemon(true);
            inputThread.start();

        }

        return hook;

    }

    /**
     * Unregisters and handles the closing of an InputHook.
     * @param uuid the uuid of this hook.
     */
    private static void stopInput(UUID uuid) {
        inputHooks.remove(uuid);
        if (inputHooks.isEmpty()) {
            stopRaw();
            try {
                reader.close();
            } catch (Exception ignored) {}
            reader = null;
            inputThread.interrupt();
            inputThread = null;
        }
    }

    /**
     * Closes all InputHooks.
     */
    public static void closeAll() {
        for (InputHook hook : inputHooks.values().toArray(new InputHook[0])) {
            hook.close();
        }
    }


}
