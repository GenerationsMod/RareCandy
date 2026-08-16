package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.renderer.launch.OpenGL;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class AppBase {
    private static final Vector4f DEFAULT_CLEAR_COLOR = new Vector4f();
    public final String title;
    protected long window;
    private int width;
    private int height;
    private final OpenGL data;

    private ImGuiImplGlfw imguiGlfw;
    private ImGuiImplGl3 imguiGl3;
    private Queue<Runnable> runnables = new ArrayDeque<>();
    private float uiScale = 1.0f;
    private float appliedUiScale = 1.0f;

    public AppBase(String title, int width, int height, OpenGL data) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public void run() {
        initWindow();
        initGL();
        initImGui();
        loop();
        cleanup();
    }

    protected void initWindow() {
        if (!glfwInit()) throw new IllegalStateException("Unable to init GLFW");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, data.majorVersion);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, data.minorVersion);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(getWidth(), getHeight(), title, NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glViewport(0, 0, getWidth(), getHeight());

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            this.width = w;
            this.height = h;
            glViewport(0, 0, w, h); // also update your OpenGL viewport
            onResize(w, h);
        });
    }

    protected void onResize(int width, int height) {
    }

    private void initImGui() {
        ImGui.createContext();
        imguiGlfw = new ImGuiImplGlfw();
        imguiGl3 = new ImGuiImplGl3();
        imguiGlfw.init(window, true);
        imguiGl3.init("#version 430");
    }

    protected abstract void initGL();

    protected void addRunnable(Runnable runnable) {
        runnables.add(runnable);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {

            var runnable = runnables.poll();

            while(runnable != null) {
                runnable.run();
                runnable = runnables.poll();
            }

            glfwPollEvents();

            var clear = clearColor();

            glClearColor(clear.x, clear.y, clear.z, clear.w);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            render();

            imguiGlfw.newFrame();
            imguiGl3.newFrame();
            ImGui.newFrame();
            applyUiScale();
            renderGui();

            ImGui.render();
            glDisable(GL_DEPTH_TEST);
            imguiGl3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(window);
        }
    }

    protected Vector4f clearColor() {
        return DEFAULT_CLEAR_COLOR;
    }

    protected abstract void renderGui();

    protected abstract void render();

    protected void setUiScale(float uiScale) {
        this.uiScale = uiScale;
    }

    private void applyUiScale() {
        ImGui.getIO().setFontGlobalScale(uiScale);

        if (uiScale != appliedUiScale) {
            ImGui.getStyle().scaleAllSizes(uiScale / appliedUiScale);
            appliedUiScale = uiScale;
        }
    }

    private void cleanup() {
        cleanupGL();
        imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    protected void cleanupGL() {}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
