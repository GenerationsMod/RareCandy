package gg.generations.rarecandy.tools;

import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private BlankTexture target;
    private Queue<Runnable> runnables = new ArrayDeque<>();

    public static void main(String[] args) throws IOException {
        new ComputeShaderDemo().run();
    }

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

            glEnable(GL_DEPTH_TEST);
            render();

            imguiGlfw.newFrame();
            imguiGl3.newFrame();
            ImGui.newFrame();
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

    private static final float[] colorArray = new float[3];

    private boolean colorEdit3(String name, Vector3f color) {
        colorArray[0] = color.x();
        colorArray[1] = color.y();
        colorArray[2] = color.z();


        var succeeded = ImGui.colorEdit3(name, colorArray);

        if(succeeded) {
            color.set(colorArray[0], colorArray[1], colorArray[2]);
        }

        return succeeded;
    }

    private void cleanup() {
        cleanupGL();
        imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    protected void cleanupGL() {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
