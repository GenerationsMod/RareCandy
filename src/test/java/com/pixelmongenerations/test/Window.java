package com.pixelmongenerations.test;

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
    private static final boolean DEBUGGING = true;

    public final String title;
    public final int width;
    public final int height;
    private long handle;

    public Window(String title, int width, int height) {
        System.loadLibrary("renderdoc");
        this.title = title;
        this.width = width;
        this.height = height;

        initGlfw();
    }

    private void collectGlDiagnostics(GLCapabilities capabilities) {
        boolean supportsLatest = capabilities.OpenGL46;
        boolean supports45 = capabilities.OpenGL45;

        /* FIXME: Make this work better. Creating capabilities with a set version causes issues because then this thinks that something isn't supported...
                if (!supportsLatest && !supports45) {
            popup(JOptionPane.ERROR_MESSAGE, "Your GPU does not support OpenGL 4.5 or 4.6 :(. Please try to update your GPU and CPU drivers.");
        }

        if (!supportsLatest) {
            popup(JOptionPane.WARNING_MESSAGE, "Your GPU does not support OpenGL 4.6. We can still but run but that isn't good for the future...");
        }*/
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

        GLFW.glfwSetErrorCallback(this::onGlfwError);

        // Setup Window Hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

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
        GLFW.glfwSetWindowSizeCallback(this.handle, (pWindow, width1, height1) -> onResize());

        // Set the OpenGL context
        GLFW.glfwMakeContextCurrent(this.handle);
        GLFW.glfwSwapInterval(0); // Dont limit the fps pls k tnx.

        // Setup OpenGL debugging if enabled
        collectGlDiagnostics(GL.createCapabilities(false));
        if (DEBUGGING) {
            //GL45C.glDebugMessageCallback(this::onGlError, MemoryUtil.NULL);
            //GL45C.glEnable(GL45C.GL_DEBUG_OUTPUT);
        }

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
        //throw new RuntimeException("Resizing the window is not supported!");
    }

/*    private void onGlError(int glSource, int glType, int id, int severity, int length, long pMessage, long userParam) {
        String source = switch (glSource) {
            case GL43C.GL_DEBUG_SOURCE_API -> "api";
            case GL43C.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "window system";
            case GL43C.GL_SHADER_COMPILER -> "shader compiler";
            case GL43C.GL_DEBUG_SOURCE_THIRD_PARTY -> "3rd party";
            case GL43C.GL_DEBUG_SOURCE_APPLICATION -> "application";
            case GL43C.GL_DEBUG_SOURCE_OTHER -> "'other'";
            default -> throw new IllegalStateException("Unexpected value: " + glSource);
        };

        String type = switch (glType) {
            case GL43C.GL_DEBUG_TYPE_ERROR -> "error";
            case GL43C.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "deprecated behaviour";
            case GL43C.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "undefined behaviour";
            case GL43C.GL_DEBUG_TYPE_PORTABILITY -> "portability";
            case GL43C.GL_DEBUG_TYPE_PERFORMANCE -> "performance";
            case GL43C.GL_DEBUG_TYPE_MARKER -> "marker";
            case GL43C.GL_DEBUG_TYPE_OTHER -> "'other'";
            default -> throw new IllegalStateException("Unexpected value: " + glType);
        };

        if(!type.equals("'other'")) {
            System.out.println("[OpenGL " + source + " " + type + "] Message: " + MemoryUtil.memUTF8(pMessage));
        }
    }*/

    private void onGlfwError(int error, long pDescription) {
        String description = MemoryUtil.memUTF8(pDescription);
        System.err.printf("An Error has Occurred! (%d%n) Description: %s%n", error, description);
    }
}
