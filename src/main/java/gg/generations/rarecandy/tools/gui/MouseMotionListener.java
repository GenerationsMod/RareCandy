package gg.generations.rarecandy.tools.gui;

import imgui.ImGui;
import org.lwjgl.glfw.GLFW;

public interface MouseMotionListener {
    default void mouseDragged(long window, double x, double y) {}
    default void mouseMoved(long window, double x, double y) {}

    default void onMouseMotion(long window, double xpos, double ypos) {
        if(ImGui.getIO().getWantCaptureMouse()) return;

        boolean dragging =
                GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (dragging) {
            mouseDragged(window, xpos, ypos);
        } else {
            mouseMoved(window, xpos, ypos);
        }
    }

    static void attach(long window, MouseMotionListener listener) {
        GLFW.glfwSetCursorPosCallback(window, listener::onMouseMotion);
    }
}
