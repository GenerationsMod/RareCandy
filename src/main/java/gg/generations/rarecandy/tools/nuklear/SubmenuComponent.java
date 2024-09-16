package gg.generations.rarecandy.tools.nuklear;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;

import static org.lwjgl.nuklear.Nuklear.*;

public class SubmenuComponent implements NuklearComponent {

    private String label;
    private NuklearComponent[] components;
    private float menuWidth;
    private float menuHeight;

    // Constructor to define the submenu's label, size, and the components inside it
    public SubmenuComponent(String label, float menuWidth, float menuHeight, NuklearComponent... components) {
        this.label = label;
        this.menuWidth = menuWidth;
        this.menuHeight = menuHeight;
        this.components = components;
    }

    @Override
    public void layout(NkContext ctx) {
        NkVec2 submenuSize = NkVec2.malloc().set(menuWidth, menuHeight);  // Define the submenu size

        // Begin the submenu with the given label
        if (nk_menu_begin_label(ctx, label, NK_TEXT_LEFT, submenuSize)) {
            // Layout the components inside the submenu (these will be vertical by default)
            nk_layout_row_dynamic(ctx, 30, 1);  // Vertical layout for submenu items
            for (NuklearComponent component : components) {
                component.layout(ctx);
            }
            nk_menu_end(ctx);  // End the submenu
        }

        submenuSize.free();  // Free allocated memory
    }

    public String getLabel() {
        return label;
    }
}
