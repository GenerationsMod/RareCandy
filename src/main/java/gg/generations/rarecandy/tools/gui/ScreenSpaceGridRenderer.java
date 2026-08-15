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

    public void render(int width, int height, PokeUtilsGui.Settings settings, StateManager stateManager) {
        if (GuiPipelines.GRID == null || settings == null || !settings.features.grid.getValue() || width <= 0 || height <= 0) {
            return;
        }

        GuiPipelines.setGridViewport(width, height);
        GuiPipelines.GRID.useProgram();
        GuiPipelines.GRID.bindGlobal();

        stateManager.toggle(true, false, true);
        drawBuffer.render();
    }

    @Override
    public void close() {
        drawBuffer.delete();
    }
}
