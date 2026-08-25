package gg.generations.rarecandy.tools.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public interface MouseWheelListener {
    void mouseWheelMoved(long window, double xoffset, double yoffset);

    static void attach(long window, MouseWheelListener listener) {
        GLFW.glfwSetScrollCallback(window, listener::mouseWheelMoved);
    }
}
