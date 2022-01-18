package cf.hydos.engine.core;

import cf.hydos.engine.rendering.RenderingEngine;
import cf.hydos.engine.rendering.Window;

public class Renderer {
    private static final long SECOND_IN_NANOSECONDS = 1_000_000_000L;

    private boolean isRunning;
    private final RenderingApplication game;
    private RenderingEngine renderingEngine;
    private final int width;
    private final int height;
    private final double frameTime;

    public Renderer(int width, int height, double framerate, RenderingApplication game) {
        this.isRunning = false;
        this.game = game;
        this.width = width;
        this.height = height;
        this.frameTime = 1.0 / framerate;
        game.SetEngine(this);
    }

    public Renderer createWindow(String title) {
        Window.CreateWindow(width, height, title);
        this.renderingEngine = new RenderingEngine();
        return this;
    }

    public Renderer start() {
        if (isRunning)
            return this;

        run();
        return this;
    }

    public void stop() {
        if (!isRunning)
            return;

        isRunning = false;
    }

    private void run() {
        isRunning = true;

        int frames = 0;
        double frameCounter = 0;

        game.init();

        double lastTime = getTime();
        double unprocessedTime = 0;

        while (isRunning) {
            boolean render = false;

            double startTime = getTime();
            double passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime;
            frameCounter += passedTime;

            while (unprocessedTime > frameTime) {
                render = true;

                unprocessedTime -= frameTime;

                if (Window.IsCloseRequested())
                    stop();

                Window.Update();
                game.Input((float) frameTime);

                game.Update((float) frameTime);

                if (frameCounter >= 1.0) {
                    System.out.println(frames);
                    frames = 0;
                    frameCounter = 0;
                }
            }
            if (render) {
                game.Render(renderingEngine);
                Window.Render();
                frames++;
            } else {
                try {
                    Thread.sleep(0, 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        clean();
    }

    private void clean() {
        Window.Dispose();
    }

    public static double getTime() {
        return (double) System.nanoTime() / (double) SECOND_IN_NANOSECONDS;
    }
}
