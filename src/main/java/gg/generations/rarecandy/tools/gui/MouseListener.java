package gg.generations.rarecandy.tools.gui;

import imgui.ImGui;
import org.lwjgl.glfw.GLFW;

public interface MouseListener {
    default void mouseClicked(long window, int button, int mods, double x, double y) {}
    default void mousePressed(long window, int button, int mods, double x, double y) {}
    default void mouseReleased(long window, int button, int mods, double x, double y) {}
    default void mouseEntered(long window, double x, double y) {}
    default void mouseExited(long window, double x, double y) {}

    default void onMouseButton(long window, int button, int action, int mods) {
        if(ImGui.getIO().getWantCaptureMouse()) return;

        double[] xpos = new double[1];
        double[] ypos = new double[1];
        GLFW.glfwGetCursorPos(window, xpos, ypos);

        if (action == GLFW.GLFW_PRESS) {
            mousePressed(window, button, mods, xpos[0], ypos[0]);
        } else if (action == GLFW.GLFW_RELEASE) {
            mouseReleased(window, button, mods, xpos[0], ypos[0]);
            mouseClicked(window, button, mods, xpos[0], ypos[0]); // emulate Swing click
        }
    }

    default void onCursorEnter(long window, boolean entered) {
        if(ImGui.getIO().getWantCaptureMouse()) return;

        double[] xpos = new double[1];
        double[] ypos = new double[1];
        GLFW.glfwGetCursorPos(window, xpos, ypos);

        if (entered) {
            mouseEntered(window, xpos[0], ypos[0]);
        } else {
            mouseExited(window, xpos[0], ypos[0]);
        }
    }

    static void attach(long window, MouseListener listener) {
        GLFW.glfwSetMouseButtonCallback(window, listener::onMouseButton);
        GLFW.glfwSetCursorEnterCallback(window, listener::onCursorEnter);
    }
}
