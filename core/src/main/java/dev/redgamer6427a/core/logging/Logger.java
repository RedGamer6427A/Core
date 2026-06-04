package dev.redgamer6427a.core.logging;

import dev.redgamer6427a.core.processing.Format;
import dev.redgamer6427a.core.processing.Parameterize;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import static dev.redgamer6427a.core.console.output.ConsoleMiniMessage.mm;

/**
 * A basic approach to logging
 */
public class Logger {

    /**
     * The sink to use for saving files.
     */
    @Setter
    private static LogSink sink = null;

    /**
     * The minimum level to log messages. If this is set to INFO, FINE messages will not appear in console output or saved logs.
     */
    @Setter
    @Getter
    private static Level minLevel = Level.INFO;

    /**
     * This Logger's class
     */
    @Getter
    private final Class<?> clazz;

    /**
     * The Logger's output consumer (default: System.out)
     */
    @Setter
    @Getter
    private static Consumer<String> out = System.out::println;

    /**
     * The Logger's error output consumer (default: System.err)
     */
    @Setter
    @Getter
    private static Consumer<String> errOut = System.err::println;

    /**
     * A protected constructor
     * @param clazz the class to use
     */
    protected Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * A factory method that creates a Logger assigned to the caller class
     * @return A logger
     */
    public static Logger create() {
        Exception exception = new Exception();
        try {
            Class<?> clazz = Class.forName(exception.getStackTrace()[1].getClassName());
            return create(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A factory method that creates a Logger
     * @param clazz the class to use
     * @return A logger
     */
    public static Logger create(Class<?> clazz) {
        return new Logger(clazz);
    }

    /**
     * The log output format
     */
    @Getter
    @Setter
    private static String logMessage = "<dark_gray>[<gray><time><dark_gray>] {<gray><thread><dark_gray>} (<gray><origin><dark_gray>) <bg_level><black><levelName><reset><levelNamePadding> <dark_gray>> <fg_level><msg>";

    /**
     * Log a message to console and sink.
     * @param level the level of the log
     * @param message a parameterized message
     * @param parameters parameters
     */
    public void log(Level level, String message, Object... parameters) {

        if (level.getSeverity() < minLevel.getSeverity()) return;

        String time = Format.formatDate(Date.from(Instant.now()), Format.DateFormats.LOGS).replaceAll("([.\\-:])", "<dark_gray>$1<gray>");
        String origin = clazz.getPackageName() + "<dark_gray>.<white>" + clazz.getSimpleName();
        String threadName = Thread.currentThread().getName();

        String levelName = level.name().toUpperCase(Locale.ROOT);
        String fgLevel = "<fg_" + level.getColor() + ">";
        String bgLevel = "<bg_" + level.getColor() + ">";

        String output = logMessage
                .replace("<time>", time)
                .replace("<origin>", origin)
                .replace("<bg_level>", bgLevel)
                .replace("<levelName>", levelName)
                .replace("<fg_level>", fgLevel)
                //no-inspect varargs
                .replace("<msg>", Parameterize.parameterize(message, true, true, parameters))
                .replace("<thread>", threadName)
                .replace("<levelNamePadding>", getLevelPadding(level))
                ;
        String rendered = mm(output);
        origin = clazz.getPackageName() + "." + clazz.getSimpleName();

        if (level.getSeverity() >= Level.ERROR.getSeverity()) {
            errOut.accept(rendered);
        } else {
            out.accept(rendered);
        }

        if (sink != null) {
            sink.write(level, threadName, origin, rendered, Parameterize.parameterize(message, false, false, (Object) parameters));
        }

    }

    /**
     * Gets a level's padding for output format
     * @param level the level to pad
     * @return the padding for that level
     */
    private static String getLevelPadding(Level level) {

        int maxLength = 0;

        for (Level l : Level.values()) {
            maxLength = Math.max(maxLength, l.name().length());
        }

        return " ".repeat(maxLength - level.name().length());

    }

    /**
     * Helper method for logging a FINEST message.
     * @param message a parameterized message.
     * @param parameters parameters
     */
    public void finest(String message, Object... parameters) {
        log(Level.FINEST, message, parameters);
    }
    /**
     * Helper method for logging a FINE message.
     * @param message a parameterized message.
     * @param parameters parameters
     */
    public void fine(String message, Object... parameters) {
        log(Level.FINE, message, parameters);
    }
    /**
     * Helper method for logging an INFO message.
     * @param message a parameterized message.
     * @param parameters parameters
     */
    public void info(String message, Object... parameters) {
        log(Level.INFO, message, parameters);
    }
    /**
     * Helper method for logging a WARNING message.
     * @param message a parameterized message.
     * @param parameters parameters
     */
    public void warning(String message, Object... parameters) {
        log(Level.WARNING, message, parameters);
    }
    /**
     * Helper method for logging an ERROR message.
     * @param message a parameterized message.
     * @param parameters parameters
     */
    public void error(String message, Object... parameters) {
        log(Level.ERROR, message, parameters);
    }
    /**
     * Helper method for logging a CRITICAL message.
     * @param message a parameterized message.
     * @param parameters parameters
     */
    public void critical(String message, Object... parameters) {
        log(Level.CRITICAL, message, parameters);
    }

    /**
     * Declare the catching of an exception in the logs.
     * @param exception the caught exception
     */
    public void catching(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage());
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\n    at ").append(element);
        }
        // causes
        Throwable cause = exception.getCause();
        while (cause != null) {
            sb.append("\nCaused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage());
            for (StackTraceElement element : cause.getStackTrace()) {
                sb.append("\n    at ").append(element);
            }
            cause = cause.getCause();
        }
        log(Level.ERROR, sb.toString());
    }

    /**
     * Declare the throwing of an exception
     * @param exception the thrown exception
     * @return that same exception for ease of use
     * @param <T> the exception class
     */
    public <T extends Throwable> T throwing(T exception) {
        log(Level.ERROR, "Throwing "+exception.getClass().getSimpleName()+": "+exception.getMessage());
        return exception; // so you can: throw log.throwing(new Exception())
    }

}
