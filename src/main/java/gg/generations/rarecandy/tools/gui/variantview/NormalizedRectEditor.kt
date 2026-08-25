package gg.generations.rarecandy.tools.gui.variantview

import gg.generations.rarecandy.tools.gui.imgui.ImVector2f
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiMouseCursor
import org.joml.Vector2f
import kotlin.math.max

class NormalizedRectEditor {
    val scale = ImVector2f()
    val offset = ImVector2f()
    var dragging: Boolean = false
    var released: Boolean = false

    private val anchor = Vector2f()
    private val delta = ImVec2()
    private val corners = FloatArray(4)
    private var areaWidth = 0f
    private var areaHeight = 0f

    fun run(areaMin: ImVec2, areaMax: ImVec2, initialScale: Vector2f?, initialOffset: Vector2f?) {
        areaWidth = areaMax.x - areaMin.x
        areaHeight = areaMax.y - areaMin.y

        scale.set(initialScale)
        offset.set(initialOffset)
        dragging = false
        released = false

        if (areaWidth <= 0f || areaHeight <= 0f) return

        screenRect(areaMin)

        ImGui.setCursorScreenPos(corners[0], corners[1])
        ImGui.invisibleButton("##move", max(1f, corners[2] - corners[0]), max(1f, corners[3] - corners[1]))
        ImGui.setItemAllowOverlap()
        handle(ImGuiMouseCursor.ResizeAll, offset)

        ImGui.setCursorScreenPos(corners[2] - HANDLE_SIZE, corners[3] - HANDLE_SIZE)
        ImGui.invisibleButton("##size", HANDLE_SIZE, HANDLE_SIZE)
        handle(ImGuiMouseCursor.ResizeNWSE, scale)

        clamp(scale, offset)
        screenRect(areaMin)
        draw(areaMin)

        ImGui.setCursorScreenPos(areaMin.x, areaMax.y)
    }

    private fun handle(cursor: Int, target: Vector2f) {
        if (ImGui.isItemHovered()) ImGui.setMouseCursor(cursor)
        if (ImGui.isItemActivated()) anchor.set(target)

        val active = ImGui.isItemActive()
        val deactivated = ImGui.isItemDeactivated()

        if (active || deactivated) {
            ImGui.getMouseDragDelta(delta, ImGuiMouseButton.Left, 0f)
            target.set(
                anchor.x + delta.x / areaWidth,
                anchor.y + delta.y / areaHeight
            )
        }

        dragging = dragging or active
        released = released or deactivated
    }

    private fun draw(areaMin: ImVec2) {
        val drawList = ImGui.getWindowDrawList()
        val rectColor = if (dragging)
            ImGui.getColorU32(1f, 0.78f, 0.16f, 1f)
        else
            ImGui.getColorU32(0.12f, 0.72f, 1f, 1f)

        drawList.pushClipRect(areaMin.x, areaMin.y, areaMin.x + areaWidth, areaMin.y + areaHeight, true)
        drawList.addRect(corners[0], corners[1], corners[2], corners[3], rectColor, 0f, 0, if (dragging) 3f else 2f)
        drawList.addRectFilled(
            corners[2] - HANDLE_SIZE,
            corners[3] - HANDLE_SIZE,
            corners[2],
            corners[3],
            ImGui.getColorU32(ImGuiCol.Text)
        )
        drawList.popClipRect()
    }

    private fun screenRect(areaMin: ImVec2) {
        corners[0] = areaMin.x + offset.x * areaWidth
        corners[1] = areaMin.y + offset.y * areaHeight
        corners[2] = corners[0] + scale.x * areaWidth
        corners[3] = corners[1] + scale.y * areaHeight
    }

    fun render(): Boolean {
        var commit = false;

        if (scale.render("Scale")) commit = true
        if (offset.render("Offset")) commit = true;
        return commit
    }

    companion object {
        private const val HANDLE_SIZE = 10f
        private const val MIN_SCALE = 0.001f

        @JvmStatic
        fun clamp(scale: Vector2f, offset: Vector2f) {
            scale.set(Math.clamp(scale.x, MIN_SCALE, 1f), Math.clamp(scale.y, MIN_SCALE, 1f))
            offset.set(Math.clamp(offset.x, 0f, 1f - scale.x), Math.clamp(offset.y, 0f, 1f - scale.y))
        }
    }
}