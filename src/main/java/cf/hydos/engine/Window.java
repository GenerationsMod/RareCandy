package cf.hydos.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

import javax.swing.*;

import java.util.Objects;

/**
 * New modern window handling, so I don't cringe looking at this codebase as much.
 */
public class Window {

    public final String title;
    public final int width;
    public final int height;
    private long handle;

    public Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;

        initGlfw();
    }

    private void collectGlDiagnostics(GLCapabilities capabilities) {
        boolean supportsLatest = capabilities.OpenGL46;
        boolean supports45 = capabilities.OpenGL45;

        if (!supportsLatest && !supports45) {
            popup(JOptionPane.ERROR_MESSAGE, "Your GPU does not support OpenGL 4.5 or 4.6 :(. Please try to update your GPU and CPU drivers.");
        }

        if (!supportsLatest) {
            popup(JOptionPane.WARNING_MESSAGE, "Your GPU does not support OpenGL 4.6. We can still but run but that isn't good for the future...");
        }
    }

    private void popup(int popupType, String message) {
        JOptionPane.showMessageDialog(null, message, "Pixelmon Renderer", popupType);

        if (popupType == JOptionPane.ERROR_MESSAGE) {
            System.exit(-1);
        }
    }

    private void initGlfw() {
        if (!GLFW.glfwInit()) {
            int errorCode = GLFW.glfwGetError(null);
            throw new RuntimeException("Failed to Initialize GLFW. Error Code: " + errorCode);
        }

        // Setup Window Hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, GLFW.GLFW_DONT_CARE);
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_CORE_PROFILE, GLFW.GLFW_TRUE);

        // Setup Window
        this.handle = GLFW.glfwCreateWindow(this.width, this.height, title, MemoryUtil.NULL, MemoryUtil.NULL);

        if (this.handle == 0) {
            // We failed to create a window. Hope its because they installed drivers & need to restart
            throw new RuntimeException("Failed to create GLFW Window! Do you need to restart?");
        }

        // Position it
        GLFWVidMode videoMode = Objects.requireNonNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()), "VideoMode was null");
        GLFW.glfwSetWindowPos(this.handle, (videoMode.width() - this.width) / 2, (videoMode.height() - this.height) / 2);

        // Setup callbacks
        GLFW.glfwSetErrorCallback(this::onError);
        GLFW.glfwSetWindowSizeCallback(this.handle, (pWindow, width1, height1) -> onResize());

        // Set the OpenGL context
        GLFW.glfwMakeContextCurrent(this.handle);
        GLFW.glfwSwapInterval(0); // Dont limit the fps pls k tnx.

        // Make sure we are ready to show the window then show it.
        collectGlDiagnostics(GL.createCapabilities());
        GLFW.glfwShowWindow(this.handle);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.handle);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.handle);
    }

    public void pollEvents() {
        GLFW.glfwPollEvents();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(this.handle);
    }

    private void onResize() {
        throw new RuntimeException("Resizing the window is not supported!");
    }

    private void onError(int error, long pDescription) {
        String description = MemoryUtil.memUTF8(pDescription);
        System.err.printf("An Error has Occurred! (%d%n) Description: %s%n", error, description);
    }
}
