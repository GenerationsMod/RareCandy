package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.components.DummyVAO;
import gg.generations.rarecandy.renderer.rendering.StateManager;
import gg.generations.rarecandy.renderer.storage.DrawBuffer;

public final class ScreenSpaceGridRenderer implements AutoCloseable {
    private final DrawBuffer drawBuffer = new DrawBuffer(16);

    public ScreenSpaceGridRenderer() {
        drawBuffer.putDraw(3, 1, 0, 0);
        drawBuffer.upload();
    }

    public void render(PokeUtilsGui.Settings settings, StateManager stateManager) {
        if (!settings.getFeatures().getGrid()) {
            return;
        }

        GuiPipelines.GRID.useProgram();
        GuiPipelines.GRID.bindGlobal();

        stateManager.toggle(false, false, true);
        drawBuffer.render();
    }

    @Override
    public void close() {
        drawBuffer.delete();
    }
}
