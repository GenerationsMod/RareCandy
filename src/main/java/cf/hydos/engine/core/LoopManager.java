package cf.hydos.engine.core;

import cf.hydos.engine.Window;
import cf.hydos.engine.rendering.RenderingEngine;

public class LoopManager {
    private static final long SECOND_IN_NANOSECONDS = 1_000_000_000L;

    private final Window window;
    private final RenderingEngine renderer;
    private final RenderingApplication application;
    private final double frameTime = 1.0 / 60; // TODO: this is very wrong and will change often.
    private boolean isRunning;

    public LoopManager(int width, int height, String title, RenderingApplication application) {
        this.isRunning = false;
        this.application = application;
        this.window = new Window(title, width, height);
        this.renderer = new RenderingEngine(this.window);
        application.setRenderer(this);
    }

    public void stop() {
        if (!isRunning)
            return;

        isRunning = false;
    }

    public void start() {
        isRunning = true;
        application.init();

        while (isRunning) {
            if (this.window.shouldClose()) {
                stop();
            }

            this.window.pollEvents();
            this.application.Input((float) frameTime);
            this.application.Update((float) frameTime);
            this.application.render(this.renderer);
            this.window.swapBuffers();
        }

        clean();
    }

    private void clean() {
        // TODO:
    }
}
