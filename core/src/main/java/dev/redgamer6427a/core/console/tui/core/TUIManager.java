package dev.redgamer6427a.core.console.tui.core;

import dev.redgamer6427a.core.console.input.KittyInputHook;
import dev.redgamer6427a.core.console.input.KittyTerminalInput;
import dev.redgamer6427a.core.console.input.TerminalInput;
import dev.redgamer6427a.core.logging.Logger;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jline.terminal.Terminal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.redgamer6427a.core.console.output.ConsoleMiniMessage.printMM;

public class TUIManager {

    private static KittyInputHook hook;
    private static ScheduledExecutorService scheduler;
    @Getter
    private static volatile View currentView;

    public static void setCurrentView(View currentView) {
        TUIManager.currentView = currentView;
        currentView.refresh();
    }

    private static int repeatMs = 100;

    public static void setRepeatMs(int repeatMs) {
        TUIManager.repeatMs = repeatMs;
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(TUIManager::repeat, 0,           // initial delay
                    repeatMs,         // period
                    TimeUnit.MILLISECONDS);

        }
    }

    public static void start() {

        try {
            TerminalInput.startRaw();

            TerminalInput.getTerminal().handle(Terminal.Signal.WINCH, signal -> currentView.refresh());

            hook = KittyTerminalInput.makeHook();
            hook.onKeyExecutor(TUIManager::keyEvent);
            hook.onMouseExecutor(TUIManager::mouseEvent);

            Runtime.getRuntime().addShutdownHook(new Thread(TUIManager::stop));
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(TUIManager::repeat, 0,           // initial delay
                    repeatMs,         // period
                    TimeUnit.MILLISECONDS);




        } catch (Exception e) {
            stop(e);
        }

    }

    private static void repeat() {
        try {
            currentView.refresh();
        } catch (Exception e) {
            stop(e);
        }
    }

    private static void keyEvent(KittyTerminalInput.KeyEvent e) {

        if (e.isPress() && e.alt() && e.ctrl() && e.shift() && e.character() == 'q') {
            stop();
        }

        currentView.receiveEvent(e);

    }

    private static void mouseEvent(KittyTerminalInput.MouseEvent e) {
        currentView.receiveEvent(e);
    }

    public static void stop() {
        stop(null);
    }

    private static final Logger logger = Logger.create();


    public static void stop(@Nullable Exception exception) {
        try {
            printMM("<main_buffer><cursor_show>");
            Logger.setOut(System.out::println);
            Logger.setErrOut(System.err::println);
            if (exception != null) {
                logger.catching(exception);
            }
            logger.info("Stopping...");
            if (scheduler != null) {
                scheduler.shutdown();
            }
            hook.close();
            logger.info("Done stopping. Bye!");
        } catch (Exception e) {
            logger.catching(e);
        }
    }
}
