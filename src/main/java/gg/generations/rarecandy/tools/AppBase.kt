package gg.generations.rarecandy.tools

import gg.generations.rarecandy.renderer.launch.OpenGL
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
import java.util.*

abstract class AppBase(val title: String, @JvmField var width: Int, @JvmField var height: Int, private val data: OpenGL) {
    @JvmField
    protected var window: Long = 0

    private lateinit var imguiGlfw: ImGuiImplGlfw
    private lateinit var imguiGl3: ImGuiImplGl3
    private val runnables = ArrayDeque<() -> Unit>()
    private var uiScale = 1.0f
    private var appliedUiScale = 1.0f

    fun run() {
        initWindow()
        initGL()
        initImGui()
        loop()
        cleanup()
    }

    protected open fun initWindow() {
        check(GLFW.glfwInit()) { "Unable to init GLFW" }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, data.majorVersion)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, data.minorVersion)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)

        window = GLFW.glfwCreateWindow(this.width, this.height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        GLFW.glfwMakeContextCurrent(window)
        GL.createCapabilities()

        GL11.glViewport(0, 0, this.width, this.height)

        GLFW.glfwSetFramebufferSizeCallback(window) { _, w, h ->
            this.width = w
            this.height = h
            GL11.glViewport(0, 0, w, h)
            onResize(w, h)
        }
    }

    protected open fun onResize(width: Int, height: Int) {}

    private fun initImGui() {
        ImGui.createContext()
        imguiGlfw = ImGuiImplGlfw()
        imguiGl3 = ImGuiImplGl3()
        imguiGlfw.init(window, true)
        imguiGl3.init("#version 430")
    }

    protected abstract fun initGL()

    protected fun addRunnable(runnable: () -> Unit) {
        runnables.add(runnable)
    }

    private fun loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            var runnable = runnables.poll()

            while (runnable != null) {
                runnable.invoke()
                runnable = runnables.poll()
            }

            GLFW.glfwPollEvents()

            val clear = clearColor()

            GL11.glClearColor(clear.x, clear.y, clear.z, clear.w)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            render()

            imguiGlfw.newFrame()
            imguiGl3.newFrame()
            ImGui.newFrame()
            applyUiScale()
            renderGui()

            ImGui.render()
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            imguiGl3.renderDrawData(ImGui.getDrawData())

            GLFW.glfwSwapBuffers(window)
        }
    }

    open fun clearColor(): Vector4f {
        return DEFAULT_CLEAR_COLOR
    }

    protected abstract fun renderGui()

    protected abstract fun render()

    protected fun setUiScale(uiScale: Float) {
        this.uiScale = uiScale
    }

    private fun applyUiScale() {
        ImGui.getIO().fontGlobalScale = uiScale

        if (uiScale != appliedUiScale) {
            ImGui.getStyle().scaleAllSizes(uiScale / appliedUiScale)
            appliedUiScale = uiScale
        }
    }

    private fun cleanup() {
        cleanupGL()
        imguiGl3.shutdown()
        imguiGlfw.shutdown()
        ImGui.destroyContext()
        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
    }

    protected open fun cleanupGL() {}

    companion object {
        private val DEFAULT_CLEAR_COLOR = Vector4f()
    }
}
