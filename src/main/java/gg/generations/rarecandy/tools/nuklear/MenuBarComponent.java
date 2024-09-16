package gg.generations.rarecandy.tools.nuklear;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;

import static org.lwjgl.nuklear.Nuklear.*;

public class MenuBarComponent implements NuklearComponent {

    @Override
    public void layout(NkContext ctx) {
        // Begin the menubar
        nk_menubar_begin(ctx);

        // Set up the row layout for the menu bar
        nk_layout_row_static(ctx, 25, 80, 1);

        NkVec2 menuSize = NkVec2.malloc().set(120, 200);  // Define the size for the dropdown menus

        // "File" menu
        if (nk_menu_begin_label(ctx, "File", NK_TEXT_LEFT, menuSize)) {
            nk_layout_row_dynamic(ctx, 30, 1);
            if (nk_menu_item_label(ctx, "Open", NK_TEXT_LEFT)) {
                System.out.println("Open File clicked");
            }
            if (nk_menu_item_label(ctx, "Save", NK_TEXT_LEFT)) {
                System.out.println("Save File clicked");
            }
            if (nk_menu_item_label(ctx, "Exit", NK_TEXT_LEFT)) {
                System.out.println("Exit clicked");
            }
            nk_menu_end(ctx);
        }

        // "Edit" menu
        if (nk_menu_begin_label(ctx, "Edit", NK_TEXT_LEFT, menuSize)) {
            nk_layout_row_dynamic(ctx, 30, 1);
            if (nk_menu_item_label(ctx, "Undo", NK_TEXT_LEFT)) {
                System.out.println("Undo clicked");
            }
            if (nk_menu_item_label(ctx, "Redo", NK_TEXT_LEFT)) {
                System.out.println("Redo clicked");
            }
            nk_menu_end(ctx);
        }

        // End the menubar
        nk_menubar_end(ctx);
    }
}