package gg.generations.rarecandy.tools.nuklear;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.*;

public class WindowComponent implements NuklearComponent {

    private String title;
    private float x;
    private float y;
    private Supplier<Number> width;
    private Supplier<Number> height;
    private NuklearComponent[] components;

    // Constructor to define the window's title, position, size, and contained components
    public WindowComponent(String title, float x, float y, Supplier<Number> width, Supplier<Number> height, NuklearComponent... components) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.components = components;
    }

    @Override
    public void layout(NkContext ctx) {
        NkRect windowRect = NkRect.malloc();

        // Begin the window
        if (nk_begin(ctx, "", nk_rect(x, y, width.get().floatValue(), height.get().floatValue(), windowRect), 0)) {
            // Loop through and layout each component inside the window
            for (NuklearComponent component : components) {
                component.layout(ctx);  // Layout each contained component
            }
        }
        // End the window
        nk_end(ctx);
    }
}