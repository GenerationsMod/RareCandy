package gg.generations.rarecandy.tools.gui;

import imgui.ImGui;
import org.lwjgl.glfw.GLFW;

public interface KeyListener {
    void keyTyped(char keyChar, int keyCode, int scancode, int mods);
    void keyPressed(int keyCode, int scancode, int mods);
    void keyReleased(int keyCode, int scancode, int mods);

    void keyHeld(int key, int scancode, int mods);

    static void attach(long window, KeyListener listener) {
        GLFW.glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantTextInput()) return;

            switch (action) {
                case GLFW.GLFW_PRESS -> listener.keyPressed(key, scancode, mods);
                case GLFW.GLFW_RELEASE -> listener.keyReleased(key, scancode, mods);
                case GLFW.GLFW_REPEAT -> listener.keyHeld(key, scancode, mods);
            }
        });

        GLFW.glfwSetCharCallback(window, (win, codepoint) -> {
            if(ImGui.getIO().getWantTextInput()) return;
            listener.keyTyped((char) codepoint, 0, 0, 0);
        });
    }
}
