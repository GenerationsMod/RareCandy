package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.tools.ImGuiImageViewer;
import imgui.ImGui;
import imgui.ImVec2;

public class ImageNode implements PixelAssetTree.Node {
    private final RareCandyCanvas.BaseMultiRenderObject model;
    private final String
            name;

    public ImageNode(RareCandyCanvas.BaseMultiRenderObject model, String name) {
        this.model = model;
        this.name = name;
    }

    @Override
    public void render() {
        if (ImGui.treeNode(name)) {
            var texture = model.images.getLayerTexture(model.imageNameToId.get(name));

            ImGuiImageViewer.drawTexture(texture, 256, 256);

//            .imageWithMouseCoords(
//                    texture.id(),
//                    texture.width(), texture.height(),
//                    256, 256,          // draw at 512×512; use 0,0 for native size
//                    0f, 1f,               // flip Y if your backend needs it
//                    1f, 0f,
//                    null, null
//            );
            ImGui.treePop();
        }
    }
}
