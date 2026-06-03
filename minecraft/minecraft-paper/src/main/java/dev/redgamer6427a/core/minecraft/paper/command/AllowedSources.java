package dev.redgamer6427a.admiral.paper.command;

import lombok.Getter;

@Getter
public enum AllowedSources {

    CONSOLE(true, false, false),
    NON_CONSOLE(false, true, true),
    COMMAND_BLOCK(false, true, false),
    NON_COMMAND_BLOCK(true, false, true),
    PLAYER(false, false, true),
    NON_PLAYER(true, true, false),
    ALL(true, true, true),
    ;

    private final boolean canConsole;
    private final boolean canCommandBlock;
    private final boolean canPlayer;


    AllowedSources(boolean canConsole, boolean canCommandBlock, boolean canPlayer) {
        this.canConsole = canConsole;
        this.canCommandBlock = canCommandBlock;
        this.canPlayer = canPlayer;
    }
}
