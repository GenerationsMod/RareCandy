package gg.generations.rarecandy.tools.gui;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;

public class BaseCanvas extends AWTGLCanvas {

    private boolean contextInitialized = false;

    public BaseCanvas() {
        super(defaultData());
        addListeners();
    }

    protected void addListeners() {
        // Add input listeners here if needed
    }

    public void setup() {
        // Set up OpenGL resources (shaders, buffers, etc.)
        System.out.println("OpenGL setup complete");
    }

    @Override
    public void initGL() {
        // Ensure that OpenGL capabilities are initialized for this context
//        if (!contextInitialized) {
            GL.createCapabilities(true);
            contextInitialized = true;
            System.out.println("OpenGL context initialized");
//        }

        // Perform any OpenGL setup here (e.g., set clear color, etc.)
        setup();
    }

    @Override
    public void render() {
        super.render();
        swapBuffers();
    }

    public void beforeRender() {
        // Placeholder for pre-render logic
    }

    public void paintGL() {
        // Ensure that OpenGL commands only run if the context is valid
//        if (contextInitialized) {
            // Clear the screen with a color
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//        } else {
//            System.err.println("Cannot call OpenGL functions; context not initialized");
//        }
    }

    public void afterRender() {
        // Placeholder for post-render logic
    }

    // Starts the rendering loop
    public void start() {
        int delay = 16; // Approx 60 FPS (1000ms / 60 = ~16.6ms per frame)
        var renderTimer = new Timer(delay, e -> {
            if (BaseCanvas.this.isValid()) {
                BaseCanvas.this.render();
            }
        });

        renderTimer.start();
    }

    // Helper method to configure the OpenGL context
    private static GLData defaultData() {
        GLData data = new GLData();
        data.profile = GLData.Profile.CORE;
        data.api = GLData.API.GL;
        data.majorVersion = 3;
        data.minorVersion = 2;
        data.forwardCompatible = true;
        return data;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple OpenGL Canvas");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create and add the OpenGL canvas
            BaseCanvas canvas = new BaseCanvas();
            canvas.start();
            frame.add(canvas);

            // Show the frame
            frame.setVisible(true);
        });
    }
}
