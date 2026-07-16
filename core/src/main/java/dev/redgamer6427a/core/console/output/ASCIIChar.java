package dev.redgamer6427a.core.console.output;

import lombok.Getter;

/**
 * Useful ASCII Characters
 */
@Getter
public enum ASCIIChar {
    // Box characters
    BOX_TOP_LEFT("box_top_left", "┌"),
    BOX_TOP_RIGHT("box_top_right", "┐"),
    BOX_BOTTOM_LEFT("box_bottom_left", "└"),
    BOX_BOTTOM_RIGHT("box_bottom_right", "┘"),
    BOX_HORIZONTAL("box_horizontal", "─"),
    BOX_VERTICAL("box_vertical", "│"),

    // Box drawing — intersections
    BOX_CROSS("box_cross", "┼"),
    BOX_T_DOWN("box_t_down", "┬"),
    BOX_T_UP("box_t_up", "┴"),
    BOX_T_RIGHT("box_t_right", "├"),
    BOX_T_LEFT("box_t_left", "┤"),

    // Double-line box
    BOX_DBL_TOP_LEFT("box_dbl_top_left", "╔"),
    BOX_DBL_TOP_RIGHT("box_dbl_top_right", "╗"),
    BOX_DBL_BOTTOM_LEFT("box_dbl_bottom_left", "╚"),
    BOX_DBL_BOTTOM_RIGHT("box_dbl_bottom_right", "╝"),
    BOX_DBL_HORIZONTAL("box_dbl_horizontal", "═"),
    BOX_DBL_VERTICAL("box_dbl_vertical", "║"),

    // Double-line intersections
    BOX_DBL_CROSS("box_dbl_cross", "╬"),
    BOX_DBL_T_DOWN("box_dbl_t_down", "╦"),
    BOX_DBL_T_UP("box_dbl_t_up", "╩"),
    BOX_DBL_T_RIGHT("box_dbl_t_right", "╠"),
    BOX_DBL_T_LEFT("box_dbl_t_left", "╣"),

    // Heavy-line box
    BOX_HVY_TOP_LEFT("box_hvy_top_left", "┏"),
    BOX_HVY_TOP_RIGHT("box_hvy_top_right", "┓"),
    BOX_HVY_BOTTOM_LEFT("box_hvy_bottom_left", "┗"),
    BOX_HVY_BOTTOM_RIGHT("box_hvy_bottom_right", "┛"),
    BOX_HVY_HORIZONTAL("box_hvy_horizontal", "━"),
    BOX_HVY_VERTICAL("box_hvy_vertical", "┃"),

    // Rounded corners
    BOX_RND_TOP_LEFT("box_rnd_top_left", "╭"),
    BOX_RND_TOP_RIGHT("box_rnd_top_right", "╮"),
    BOX_RND_BOTTOM_LEFT("box_rnd_bottom_left", "╰"),
    BOX_RND_BOTTOM_RIGHT("box_rnd_bottom_right", "╯"),
    ;

    private final String name;
    private final String value;

    ASCIIChar(String name, String value) {
        this.name = name;
        this.value = value;
    }


}