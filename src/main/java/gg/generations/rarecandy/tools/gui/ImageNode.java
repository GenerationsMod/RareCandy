package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.tools.ImGuiImageViewer;
import imgui.ImGui;
import imgui.ImVec2;

public class ImageNode implements PixelAssetTree.Node {
    private final RareCandyCanvas.ToggleableMultiRenderObject model;
    private final String
            name;

    public ImageNode(RareCandyCanvas.ToggleableMultiRenderObject model, String name) {
        this.model = model;
        this.name = name;
    }

    @Override
    public void render() {
        if (ImGui.treeNode(name)) {
            var texture = model.images.getLayerTexture(model.imageNameToId.get(name));

            ImVec2 uv = new ImVec2();
            ImVec2 px = new ImVec2();
            boolean hovered = ImGuiImageViewer.imageWithMouseCoords(
                    texture.id(),
                    texture.width(), texture.height(),
                    256, 256,          // draw at 512×512; use 0,0 for native size
                    0f, 1f,               // flip Y if your backend needs it
                    1f, 0f,
                    uv, px
            );
            ImGui.treePop();
        }
    }
}
