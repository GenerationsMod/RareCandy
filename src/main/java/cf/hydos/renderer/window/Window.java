package cf.hydos.renderer.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_TRUE;

public class Window {
    private static Window instance;

    private final long pWindow;
    private final int width;
    private final int height;

    public Window(String title, int width, int height) {
        instance = this;

        glfwInit();
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.width = width;
        this.height = height;
        this.pWindow = glfwCreateWindow(width, height, title, 0, 0);
        glfwMakeContextCurrent(this.pWindow);
        GL.createCapabilities();
        glfwShowWindow(this.pWindow);
    }

    public void run(Runnable runnable) {
        while (!glfwWindowShouldClose(this.pWindow)) {
            runnable.run();
            glfwPollEvents();
            glfwSwapBuffers(this.pWindow);
        }
    }

    public void close() {
        glfwDestroyWindow(this.pWindow);
    }

    public static Window getInstance() {
        return instance;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
