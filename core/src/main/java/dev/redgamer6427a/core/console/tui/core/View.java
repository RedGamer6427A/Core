package dev.redgamer6427a.core.console.tui.core;

import dev.redgamer6427a.core.console.input.KittyTerminalInput;
import dev.redgamer6427a.core.console.input.RawTerminalInput;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class View {

    protected List<View> children = new ArrayList<>();

    protected int width;
    protected int height;

    @SneakyThrows
    public void refresh() {
        width = RawTerminalInput.getTerminal().getWidth();
        height = RawTerminalInput.getTerminal().getHeight();
        String view = redraw(new TerminalBuilder(width, height)).render(1,1);
        System.out.print(view);
        Files.writeString(Path.of("tmp/tb_debug.txt"), view);
    }

    protected abstract void keyEvent(KittyTerminalInput.KeyEvent e);
    protected abstract void mouseEvent(KittyTerminalInput.MouseEvent e);

    public abstract TerminalBuilder redraw(TerminalBuilder tb);

    public void receiveEvent(Object o) {
        if (o instanceof KittyTerminalInput.KeyEvent) {
            keyEvent((KittyTerminalInput.KeyEvent) o);
            children.forEach(v -> v.receiveEvent(o));
        } else if (o instanceof KittyTerminalInput.MouseEvent) {
            mouseEvent((KittyTerminalInput.MouseEvent) o);
            children.forEach(v -> v.receiveEvent(o));
        }
    }

}
