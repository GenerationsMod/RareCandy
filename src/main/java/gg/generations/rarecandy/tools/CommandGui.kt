package gg.generations.rarecandy.tools

import gg.generations.rarecandy.renderer.launch.OpenGL
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW

class CommandGui(private val commands: List<Command>, private val args: Array<String>) : AppBase("Command GUI", 520, 40 + commands.size * 34, OpenGL()) {
    private var chosen: Command? = null

    override fun initGL() {
        // runs after initWindow(), so the handle exists
        GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)
    }

    override fun render() {}

    override fun renderGui() {
        ImGui.setNextWindowPos(0f, 0f)
        ImGui.setNextWindowSize(width.toFloat(), height.toFloat())
        ImGui.begin(
            "##commands", (ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize
                    or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoCollapse
                    or ImGuiWindowFlags.NoBringToFrontOnFocus or ImGuiWindowFlags.NoSavedSettings)
        )

        for (c in commands) {
            if (ImGui.button(c.name, 180f, 0f)) {
                chosen = c
                GLFW.glfwSetWindowShouldClose(window, true)
            }
            ImGui.sameLine()
            ImGui.textWrapped(c.description)
        }
        ImGui.end()
    }

    fun launch() {
        run() // blocks; cleanup() calls glfwTerminate()
        chosen?.consumer?.accept(args)
    }
}