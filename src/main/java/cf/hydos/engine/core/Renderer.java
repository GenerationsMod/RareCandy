package cf.hydos.engine.core;

import cf.hydos.engine.rendering.RenderingEngine;
import cf.hydos.engine.rendering.Window;

public class Renderer {
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

    public void title(String title) {
        Window.CreateWindow(width, height, title);
        this.renderingEngine = new RenderingEngine();
    }

    public void start() {
        if (isRunning)
            return;

        Run();
    }

    public void Stop() {
        if (!isRunning)
            return;

        isRunning = false;
    }

    private void Run() {
        isRunning = true;

        int frames = 0;
        double frameCounter = 0;

        game.init();

        double lastTime = Time.GetTime();
        double unprocessedTime = 0;

        while (isRunning) {
            boolean render = false;

            double startTime = Time.GetTime();
            double passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime;
            frameCounter += passedTime;

            while (unprocessedTime > frameTime) {
                render = true;

                unprocessedTime -= frameTime;

                if (Window.IsCloseRequested())
                    Stop();

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

        CleanUp();
    }

    private void CleanUp() {
        Window.Dispose();
    }

    public RenderingEngine GetRenderingEngine() {
        return renderingEngine;
    }
}
