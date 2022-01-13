package cf.hydos.renderer.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_TRUE;

public class Window {
    private static Window instance;

    private final List<Integer> keysPressed = new ArrayList<>();
    private final long pWindow;
    private final int width;
    private final int height;
    private double mouseX;
    private double mouseY;
    private double lastMouseX;
    private double lastMouseY;

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

        glfwSetKeyCallback(this.pWindow, (window, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW_PRESS -> keysPressed.add(key);
                case GLFW_RELEASE -> keysPressed.remove((Integer) key);
            }
        });

        glfwSetMouseButtonCallback(this.pWindow, (window, button, action, mods) -> {
            switch (action) {
                case GLFW_PRESS -> keysPressed.add(button);
                case GLFW_RELEASE -> keysPressed.remove((Integer) button);
            }
        });

        glfwShowWindow(this.pWindow);
    }

    public static Window getInstance() {
        return instance;
    }

    public boolean isKeyPressed(int keyId) {
        return this.keysPressed.contains(keyId);
    }

    public void run(Runnable runnable) {
        while (!glfwWindowShouldClose(this.pWindow)) {
            runnable.run();
            glfwPollEvents();
            glfwSwapBuffers(this.pWindow);

            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer pX = stack.doubles(1);
                DoubleBuffer pY = stack.doubles(1);
                glfwGetCursorPos(this.pWindow, pX, pY);

                this.lastMouseX = this.mouseX;
                this.lastMouseY = this.mouseY;
                this.mouseX = pX.get(0);
                this.mouseY = pY.get(0);
            }
        }
    }

    public void close() {
        glfwDestroyWindow(this.pWindow);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getDx() {
        return this.mouseX - this.lastMouseX;
    }

    public double getDy() {
        return this.mouseY - this.lastMouseY;
    }
}
