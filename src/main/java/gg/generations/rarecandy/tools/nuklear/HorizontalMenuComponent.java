package gg.generations.rarecandy.tools.nuklear;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memFree;

public class HorizontalMenuComponent implements NuklearComponent {
    private SubmenuComponent[] submenus;
    private NkUserFont font;
    private float menuHeight;

    // Constructor to define the menu's label, font for width calculation, height, and submenus
    public HorizontalMenuComponent(NkUserFont font, float menuHeight, SubmenuComponent... submenus) {
        this.font = font;
        this.menuHeight = menuHeight;
        this.submenus = submenus;
    }

    @Override
    public void layout(NkContext ctx) {
        // Start the horizontal menu
        if (nk_menu_begin_label(ctx, "", NK_TEXT_LEFT, NkVec2.malloc().set(300, menuHeight))) {
            // Begin a custom layout row with the total height and a dynamic number of submenus
            nk_layout_row_begin(ctx, NK_STATIC, menuHeight, submenus.length);

            // Loop through each submenu and calculate its width based on its label text (Java String)
            for (SubmenuComponent submenu : submenus) {
                // Convert Java String to ByteBuffer (UTF-8)
                ByteBuffer labelTextBuffer = MemoryUtil.memASCII(submenu.getLabel());

                // Calculate the text width based on the font and label length
                float textWidth = font.width().invoke(
                        font.userdata().address(),     // Handle to the font
                        menuHeight,                    // The height of the text (font size)
                        memAddress(labelTextBuffer),   // Pointer to the UTF-8 encoded string
                        submenu.getLabel().length()    // Length of the string
                );

                // Add some padding or space between the items
                float paddedWidth = textWidth + 20;  // 20 pixels of padding

                // Push each submenu into the layout row with the calculated width
                nk_layout_row_push(ctx, paddedWidth);

                // Render the submenu
                submenu.layout(ctx);

                // Free the ByteBuffer after use to avoid memory leaks
                memFree(labelTextBuffer);
            }

            nk_layout_row_end(ctx);  // End the layout row
            nk_menu_end(ctx);  // End the horizontal menu
        }
    }
}