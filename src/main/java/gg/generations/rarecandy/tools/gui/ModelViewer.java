package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.tools.AppBase;
import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.callback.ImGuiFileDialogPaneFun;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import org.joml.Vector2d;

public class ModelViewer extends AppBase implements MouseMotionListener {
    private Vector2d cursor = new Vector2d();

    public static void main(String[] args) {
        new ModelViewer().run();
    }

    public ModelViewer() {
        super("Viewer", 512, 512, new OpenGL());
    }

    @Override
    protected void initGL() {
        MouseMotionListener.attach(window, this);
    }

    @Override
    public void mouseMoved(long window, double x, double y) {
        cursor.x = x;
        cursor.y = y;
    }

    @Override
    protected void renderGui() {
// Call this every frame (after ImGui.newFrame())
        ImGui.dockSpaceOverViewport(ImGui.getMainViewport());

// Global main menu bar
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("New", "Ctrl+N")) {
                    // Handle New
                }
                if (ImGui.menuItem("Open...", "Ctrl+O")) {
                    // Handle Open
                }
                ImGui.separator();
                if (ImGui.menuItem("Exit")) {
                    // Handle Exit
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Edit")) {
                if (ImGui.menuItem("Undo", "Ctrl+Z")) { }
                if (ImGui.menuItem("Redo", "Ctrl+Y")) { }
                ImGui.separator();
                if (ImGui.menuItem("Preferences...")) {
                    // Open preferences window
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Help")) {
                if (ImGui.menuItem("About")) {
                    // Show about popup
                }
                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }

// Example docked windows
        ImGui.begin("Inspector");
        ImGui.text("This is dockable.");
        ImGui.end();

        ImGui.begin("Console");
        ImGui.text("Logs go here.");
        ImGui.end();
    }

    @Override
    protected void render() {

    }
}
