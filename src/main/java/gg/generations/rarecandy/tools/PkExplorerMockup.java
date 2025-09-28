package gg.generations.rarecandy.tools;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

import static imgui.internal.ImGui.*;

public class PkExplorerMockup {

    private boolean initialized = false;
    private int dockspaceId;

    // Persisted node IDs so we can force-dock windows every frame
    private int nodeLeft   = 0;
    private int nodeRight  = 0;
    private int nodeDetails = 0;
    private int nodePreview = 0;

    public void render() {
        // Host window covering the full viewport
        final int hostFlags = ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove | ImGuiWindowFlags.MenuBar;

        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY(), ImGuiCond.Always);
        ImGui.begin("PK Explorer (Host)", hostFlags);

        dockspaceId = ImGui.getID("PkExplorerDockspace");

        // Build the dock layout ONCE
        if (!initialized) {
            dockBuilderRemoveNode(dockspaceId);
            dockBuilderAddNode(dockspaceId, ImGuiDockNodeFlags.None);

            // Split root into Left (Explorer) and Right (Details+Preview)
            ImInt left = new ImInt();
            ImInt right = new ImInt();
            dockBuilderSplitNode(dockspaceId, ImGuiDir.Left, 0.26f, left, right);

            // Split Right into Details (top) and Preview (bottom)
            ImInt bottom = new ImInt();
            ImInt top    = new ImInt();
            // Down split returns [out_id_at_dir=bottom, out_id_at_opposite=top]
            dockBuilderSplitNode(right.get(), ImGuiDir.Down, 0.52f, bottom, top);

            nodeLeft    = left.get();
            nodeRight   = right.get();
            nodePreview = bottom.get();
            nodeDetails = top.get();

            // Pre-dock windows by name
            dockBuilderDockWindow("Explorer", nodeLeft);
            dockBuilderDockWindow("Details",  nodeDetails);
            dockBuilderDockWindow("Preview",  nodePreview);

            dockBuilderFinish(dockspaceId);
            initialized = true;
        }

        // Make the docked layout NON-detachable and NON-splittable
        final int dockFlags = ImGuiDockNodeFlags.NoUndocking | ImGuiDockNodeFlags.NoDockingSplit
                | ImGuiDockNodeFlags.AutoHideTabBar; // optional: hide tabs when single window
        ImGui.dockSpace(dockspaceId, 0, 0, dockFlags);

        // Menu (optional)
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("Open...");
                ImGui.menuItem("Exit");
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }

        ImGui.end(); // Host

        // FORCE each window to its node EVERY FRAME (prevents any drifting/floating)
        if (nodeLeft != 0)    ImGui.setNextWindowDockID(nodeLeft,    ImGuiCond.Always);
        ImGui.begin("Explorer", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse);
        // --- Explorer contents ---
        if (ImGui.treeNode("arceus.pk")) {
            ImGui.selectable("config.json");
            if (ImGui.treeNode("animations")) {
                ImGui.selectable("idle.anim");
                ImGui.selectable("run.anim");
                ImGui.treePop();
            }
            if (ImGui.treeNode("images")) {
                ImGui.selectable("diffuse.png");
                ImGui.selectable("mask.png");
                ImGui.treePop();
            }
            if (ImGui.treeNode("variants")) {
                ImGui.selectable("base.json");
                ImGui.selectable("shiny.json");
                ImGui.treePop();
            }
            ImGui.treePop();
        }
        ImGui.end();

        if (nodeDetails != 0) ImGui.setNextWindowDockID(nodeDetails, ImGuiCond.Always);
        ImGui.begin("Details", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse);
        // --- Details contents ---
        ImGui.text("Name: Bulbasaur");
        ImGui.text("Dex ID: #001");
        ImGui.text("Type: Grass / Poison");
        ImGui.separator();
        ImGui.text("Model: model.glb");
        ImGui.text("Textures: diffuse.png, mask.png");
        ImGui.end();

        if (nodePreview != 0) ImGui.setNextWindowDockID(nodePreview, ImGuiCond.Always);
        ImGui.begin("Preview", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar);
        // --- Preview contents ---
        ImGui.text("Preview Area");
        ImGui.separator();
        ImGui.text("[Render your FBO/texture here via ImGui.image(...)]");
        ImGui.end();
    }
}