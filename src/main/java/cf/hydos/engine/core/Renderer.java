package cf.hydos.engine.core;

import cf.hydos.engine.rendering.RenderingEngine;
import cf.hydos.engine.rendering.Window;

public class Renderer {
    private boolean m_isRunning;
    private final RenderingApplication m_game;
    private RenderingEngine m_renderingEngine;
    private final int m_width;
    private final int m_height;
    private final double m_frameTime;

    public Renderer(int width, int height, double framerate, RenderingApplication game) {
        this.m_isRunning = false;
        this.m_game = game;
        this.m_width = width;
        this.m_height = height;
        this.m_frameTime = 1.0 / framerate;
        game.SetEngine(this);
    }

    public void title(String title) {
        Window.CreateWindow(m_width, m_height, title);
        this.m_renderingEngine = new RenderingEngine();
    }

    public void start() {
        if (m_isRunning)
            return;

        Run();
    }

    public void Stop() {
        if (!m_isRunning)
            return;

        m_isRunning = false;
    }

    private void Run() {
        m_isRunning = true;

        int frames = 0;
        double frameCounter = 0;

        m_game.init();

        double lastTime = Time.GetTime();
        double unprocessedTime = 0;

        while (m_isRunning) {
            boolean render = false;

            double startTime = Time.GetTime();
            double passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime;
            frameCounter += passedTime;

            while (unprocessedTime > m_frameTime) {
                render = true;

                unprocessedTime -= m_frameTime;

                if (Window.IsCloseRequested())
                    Stop();

                Window.Update();
                m_game.Input((float) m_frameTime);

                m_game.Update((float) m_frameTime);

                if (frameCounter >= 1.0) {
                    System.out.println(frames);
                    frames = 0;
                    frameCounter = 0;
                }
            }
            if (render) {
                m_game.Render(m_renderingEngine);
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
        return m_renderingEngine;
    }
}
