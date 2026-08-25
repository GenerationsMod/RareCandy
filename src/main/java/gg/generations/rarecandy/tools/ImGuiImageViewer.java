//package gg.generations.rarecandy.tools;// imgui-java (SpaiR) — minimal “image viewer with mouse coords” demo.
//// Draws an image, and when hovered shows UV (0..1) and pixel coords in a tooltip.
//// Library: io.github.spair:imgui-java (plus your backend, e.g., imgui-lwjgl3)
//import gg.generations.rarecandy.renderer.textures.ITexture;
//import imgui.ImGui;
//import imgui.ImGuiIO;
//import imgui.ImVec2;
//import imgui.flag.ImGuiCol;
//import imgui.flag.ImGuiHoveredFlags;
//public final class ImGuiImageViewer {
//    private static final int DEFAULT_MISSING_TEXTURE_SIZE = 256;
//    private static final String MISSING_TEXTURE_LABEL = "No texture";
//
//    private ImGuiImageViewer() {}
//
//    /**
//     * @param textureId   backend texture handle passed to ImGui.image(...)
//     * @param texW,texH   original texture size in pixels
//     * @param drawW,drawH size on screen (pass 0 to use native)
//     * @param uv0x,uv0y   sampled UV top-left (or start)
//     * @param uv1x,uv1y   sampled UV bottom-right (or end); allow flipping (e.g., (1,0) vs (0,1))
//     * @param outUv       writes (u,v) if non-null
//     * @param outPx       writes (x,y) pixel coords if non-null
//     * @return            true if mouse is over the image this frame
//     */
//    public static boolean imageWithMouseCoords(
//            int textureId,
//            float texW, float texH,
//            float drawW, float drawH,
//            float uv0x, float uv0y,
//            float uv1x, float uv1y,
//            ImVec2 outUv,
//            ImVec2 outPx
//    ) {
//        if (drawW <= 0f) drawW = texW;
//        if (drawH <= 0f) drawH = texH;
//
//        ImGui.image(textureId, drawW, drawH, uv0x, uv0y, uv1x, uv1y);
//
//        // Allow reads even while dragging another item over us (optional)
//        if (!ImGui.isItemHovered(ImGuiHoveredFlags.AllowWhenBlockedByActiveItem)) return false;
//
//        // Screen-space rect of the last item (the image)
//        ImVec2 pMin = new ImVec2();
//        ImVec2 pMax = new ImVec2();
//        ImGui.getItemRectMin(pMin);
//        ImGui.getItemRectMax(pMax);
//
//        // Mouse (screen space)
//        ImGuiIO io = ImGui.getIO();
//        float mx = io.getMousePosX();
//        float my = io.getMousePosY();
//
//        // Relative 0..1 across drawn rect (clamped)
//        float relX = clamp01((mx - pMin.x) / (pMax.x - pMin.x));
//        float relY = clamp01((my - pMin.y) / (pMax.y - pMin.y));
//
//        // Map to UVs (handles flipped axes via (uv1-uv0))
//        float u = uv0x + relX * (uv1x - uv0x);
//        float v = uv0y + relY * (uv1y - uv0y);
//
//        // Map to texture pixel coords
//        float px = u * texW;
//        float py = v * texH;
//
//        if (outUv != null) { outUv.x = u; outUv.y = v; }
//        if (outPx != null) { outPx.x = px; outPx.y = py; }
//        return true;
//    }
//
//    private static float clamp01(float v) {
//        return v < 0f ? 0f : (Math.min(v, 1f));
//    }
//
//    // -----------------------------------------------------------------------------
//    // Example usage inside your frame:
//    // -----------------------------------------------------------------------------
//    public static void
//    drawDemoWindow(int textureId, int texW, int texH) {
//        ImGui.begin("Image Viewer");
//
//        ImVec2 uv = new ImVec2();
//        ImVec2 px = new ImVec2();
//        boolean hovered = imageWithMouseCoords(
//                textureId,
//                texW, texH,
//                1024f, 1024f,          // draw at 512×512; use 0,0 for native size
//                0f, 1f,               // flip Y if your backend needs it
//                1f, 0f,
//                uv, px
//        );
//        if (hovered) {
//            ImGui.beginTooltip();
//            ImGui.text(String.format("UV: (%.4f, %.4f)", uv.x, uv.y));
//            ImGui.text(String.format("PX: (%.0f, %.0f)", px.x, px.y));
//            ImGui.endTooltip();
//        }
//        ImGui.end();
//    }
//
//    public static void drawTexture(ITexture texture, int drawW, int drawH) {
//        if (texture == null) {
//            drawMissingTexture(drawW, drawH);
//            return;
//        }
//
//        if (drawW <= 0) drawW = texture.width;
//        if (drawH <= 0) drawH = texture.height;
//
//        ImGui.image(
//                texture.id,
//                drawW, drawH,
//                0f, 0f,
//                1f, 1f);
//    }
//
//    private static void drawMissingTexture(int drawW, int drawH) {
//        if (drawW <= 0) drawW = DEFAULT_MISSING_TEXTURE_SIZE;
//        if (drawH <= 0) drawH = DEFAULT_MISSING_TEXTURE_SIZE;
//
//        ImVec2 pos = ImGui.getCursorScreenPos();
//        float maxX = pos.x + drawW;
//        float maxY = pos.y + drawH;
//
//        var drawList = ImGui.getWindowDrawList();
//        drawList.addRectFilled(pos.x, pos.y, maxX, maxY, ImGui.getColorU32(ImGuiCol.FrameBg));
//        drawList.addRect(pos.x, pos.y, maxX, maxY, ImGui.getColorU32(ImGuiCol.Border));
//
//        ImGui.dummy(drawW, drawH);
//    }
//
//}
