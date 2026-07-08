package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.IVariantDetails;
import gg.generations.rarecandy.pokeutils.IVariantParent;
import gg.generations.rarecandy.renderer.animation.ITransform;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.tools.ImGuiImageViewer;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import org.joml.Vector2f;

import java.util.LinkedHashMap;
import java.util.Map;

public class VariantView {
    private static final int TEXTURE_PREVIEW_SIZE = 256;
    private static final int TEXTURE_PREVIEW_COLUMNS = 2;
    private static final int TEXTURE_PREVIEW_SLOTS = 4;
    private static final String[] IMAGE_NAMES = new String[] { "Diffuse", "Layer", "Mask", "Emission" };
    private static final float HANDLE_SIZE = 10f;
    private static final float MIN_SCALE = 0.001f;

    private final PokeUtilsGui pokeUtilsGui;
    private final Vector2f dragStartScale = new Vector2f();
    private final Vector2f dragStartOffset = new Vector2f();
    private DragMode dragMode = DragMode.NONE;
    private ITransform dragTransform;
    private int dragMeshId = -1;
    private int dragVariantId = -1;
    private int dragSlot = -1;
    private float dragStartMouseX;
    private float dragStartMouseY;

    public VariantView(PokeUtilsGui pokeUtilsGui) {
        this.pokeUtilsGui = pokeUtilsGui;
    }

    public void render() {
        var canvas = pokeUtilsGui.canvas;

        if(canvas.loadedModel == null) return;

        var model = canvas.loadedModel;
        var meshId = canvas.selected.getMeshId();
        var variantId = canvas.selected.getVariantId();
        if (!isValidSelection(model, meshId, variantId)) return;

        var variant = model.getVariant(meshId, variantId);
        var material = model.materials[variant.material()];
        var texturearray = model.images;
        ITexture[] images = new ITexture[TEXTURE_PREVIEW_SLOTS];
        int[] textureIds = material.images();
        for (int slot = 0; slot < images.length && slot < textureIds.length; slot++) {
            int textureId = textureIds[slot];
            if (textureId < 0 || textureId >= texturearray.getLayerCount()) continue;
            images[slot] = texturearray.getLayerTexture(textureId);
        }

        ImGui.begin("Variant");

        if (ImGui.beginTable("variantTextures", TEXTURE_PREVIEW_COLUMNS)) {
            for (int i = 0; i < TEXTURE_PREVIEW_SLOTS; i++) {
                ImGui.tableNextColumn();
                ImGui.pushID(i);
                ImGui.text(IMAGE_NAMES[i]);
                if (drawTransformEditor(model, meshId, variantId, i, textureAt(images, i), variant.transform()[i])) {
                    queueVariantRebuild();
                }
                ImGui.popID();
            }

            ImGui.endTable();
        }

        ImGui.end();
    }

    private boolean drawTransformEditor(
            RareCandyCanvas.BaseMultiRenderObject model,
            int meshId,
            int variantId,
            int slot,
            ITexture texture,
            ITransform resolvedTransform) {
        ImGuiImageViewer.drawTexture(texture, TEXTURE_PREVIEW_SIZE, TEXTURE_PREVIEW_SIZE);

        ImVec2 imageMin = new ImVec2();
        ImVec2 imageMax = new ImVec2();
        ImGui.getItemRectMin(imageMin);
        ImGui.getItemRectMax(imageMax);

        ITransform transform = resolvedTransform != null ? resolvedTransform : ITransform.DEFAULT;
        Rect transformRect = transformRect(transform, imageMin, imageMax);

        ImGui.setCursorScreenPos(imageMin.x, imageMin.y);
        ImGui.invisibleButton("##transformHitbox", imageMax.x - imageMin.x, imageMax.y - imageMin.y);

        boolean hovered = ImGui.isItemHovered();
        DragMode hoveredMode = hovered ? dragModeForMouse(transformRect) : DragMode.NONE;

        if (hoveredMode == DragMode.MOVE) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeAll);
        } else if (hoveredMode == DragMode.RESIZE) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeNWSE);
        }

        if (hovered && hoveredMode != DragMode.NONE && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            beginDrag(model, meshId, variantId, slot, hoveredMode, transform);
        }

        boolean active = isActiveDrag(meshId, variantId, slot);
        boolean changed = updateDrag(meshId, variantId, slot, imageMax.x - imageMin.x, imageMax.y - imageMin.y);
        active = isActiveDrag(meshId, variantId, slot);
        if (active) {
            transformRect = transformRect(dragTransform, imageMin, imageMax);
        }

        drawTransformRect(transformRect, imageMin, imageMax, active);
        changed |= drawTransformInputs(model, meshId, variantId, slot, active ? dragTransform : transform);
        return changed;
    }

    private boolean drawTransformInputs(
            RareCandyCanvas.BaseMultiRenderObject model,
            int meshId,
            int variantId,
            int slot,
            ITransform displayTransform) {
        float[] scale = new float[] { displayTransform.scale().x, displayTransform.scale().y };
        float[] offset = new float[] { displayTransform.offset().x, displayTransform.offset().y };
        boolean changed = false;

        if (ImGui.inputFloat2("Scale", scale)) {
            ITransform editable = ensureEditableTransform(model, meshId, variantId, slot, displayTransform);
            changed |= setTransformClamped(editable, scale[0], scale[1], editable.offset().x, editable.offset().y);
            displayTransform = editable;
        }

        if (ImGui.inputFloat2("Offset", offset)) {
            ITransform editable = ensureEditableTransform(model, meshId, variantId, slot, displayTransform);
            changed |= setTransformClamped(editable, editable.scale().x, editable.scale().y, offset[0], offset[1]);
        }

        return changed;
    }

    private void beginDrag(
            RareCandyCanvas.BaseMultiRenderObject model,
            int meshId,
            int variantId,
            int slot,
            DragMode mode,
            ITransform resolvedTransform) {
        dragTransform = ensureEditableTransform(model, meshId, variantId, slot, resolvedTransform);
        dragMode = mode;
        dragMeshId = meshId;
        dragVariantId = variantId;
        dragSlot = slot;
        dragStartScale.set(dragTransform.scale());
        dragStartOffset.set(dragTransform.offset());
        dragStartMouseX = ImGui.getIO().getMousePosX();
        dragStartMouseY = ImGui.getIO().getMousePosY();
    }

    private boolean updateDrag(int meshId, int variantId, int slot, float imageWidth, float imageHeight) {
        if (!isActiveDrag(meshId, variantId, slot)) return false;

        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            endDrag();
            return false;
        }

        float deltaU = (ImGui.getIO().getMousePosX() - dragStartMouseX) / imageWidth;
        float deltaScreenY = (ImGui.getIO().getMousePosY() - dragStartMouseY) / imageHeight;

        if (dragMode == DragMode.MOVE) {
            return setTransformClamped(
                    dragTransform,
                    dragStartScale.x,
                    dragStartScale.y,
                    dragStartOffset.x + deltaU,
                    dragStartOffset.y - deltaScreenY);
        } else if (dragMode == DragMode.RESIZE) {
            float left = dragStartOffset.x;
            float top = dragStartOffset.y + dragStartScale.y;
            float right = dragStartOffset.x + dragStartScale.x + deltaU;
            float bottom = dragStartOffset.y - deltaScreenY;

            float newScaleX = Math.max(MIN_SCALE, right - left);
            float newScaleY = Math.max(MIN_SCALE, top - bottom);
            return setTransformClamped(
                    dragTransform,
                    newScaleX,
                    newScaleY,
                    left,
                    top - newScaleY);
        }

        return false;
    }

    private void endDrag() {
        dragMode = DragMode.NONE;
        dragTransform = null;
        dragMeshId = -1;
        dragVariantId = -1;
        dragSlot = -1;
    }

    private boolean isActiveDrag(int meshId, int variantId, int slot) {
        return dragMode != DragMode.NONE
                && dragTransform != null
                && dragMeshId == meshId
                && dragVariantId == variantId
                && dragSlot == slot;
    }

    private ITransform ensureEditableTransform(
            RareCandyCanvas.BaseMultiRenderObject model,
            int meshId,
            int variantId,
            int slot,
            ITransform resolvedTransform) {
        var target = editableTarget(model, variantId);
        if (target == null || target.details() == null) return copyTransform(resolvedTransform);

        String meshName = model.names.meshes().get(meshId);
        IVariantDetails details = target.details().get(meshName);
        ITransformSet transformSet = details != null ? details.transform() : null;
        ITransform existing = transformAt(transformSet, slot);

        if (existing != null) return existing;

        ITransform created = copyTransform(resolvedTransform);
        ITransformSet replacementTransformSet = replaceTransform(transformSet, slot, created);

        target.details().put(meshName, replaceDetails(model, meshId, variantId, details, replacementTransformSet, target.defaultVariant()));
        return created;
    }

    private IVariantDetails replaceDetails(
            RareCandyCanvas.BaseMultiRenderObject model,
            int meshId,
            int variantId,
            IVariantDetails details,
            ITransformSet transformSet,
            boolean defaultVariant) {
        String material = details != null ? details.material() : null;
        if (defaultVariant && material == null) {
            material = model.names.materials().get(model.getVariant(meshId, variantId).material());
        }

        return IModelConfig.Factory.ACTIVE_FACTORY.createVariantDetails(
                material,
                details != null ? details.effect() : null,
                details != null ? details.paradox() : null,
                details != null ? details.hide() : null,
                transformSet);
    }

    private EditTarget editableTarget(RareCandyCanvas.BaseMultiRenderObject model, int variantId) {
        var config = pokeUtilsGui.canvas.config;
        if (config == null) return null;

        String variantName = variantName(model, variantId);
        Map<String, IVariantParent> variants = config.variants();
        if (variantName != null && variants != null && variants.containsKey(variantName)) {
            IVariantParent parent = variants.get(variantName);
            Map<String, IVariantDetails> details = parent != null && parent.details() != null
                    ? parent.details()
                    : new LinkedHashMap<>();

            if (parent == null || parent.details() == null) {
                variants.put(variantName, IModelConfig.Factory.ACTIVE_FACTORY.createVariantParent(
                        parent != null ? parent.inherits() : null,
                        details));
            }

            return new EditTarget(details, false);
        }

        return new EditTarget(config.defaultVariant(), true);
    }

    private static ITransform copyTransform(ITransform transform) {
        ITransform source = transform != null ? transform : ITransform.DEFAULT;
        return IModelConfig.Factory.ACTIVE_FACTORY.createTransform(
                new Vector2f(source.scale()),
                new Vector2f(source.offset()));
    }

    private static ITransformSet replaceTransform(ITransformSet transformSet, int slot, ITransform transform) {
        ITransform diffuse = transformSet != null ? transformSet.diffuse() : null;
        ITransform layer = transformSet != null ? transformSet.layer() : null;
        ITransform mask = transformSet != null ? transformSet.mask() : null;
        ITransform emission = transformSet != null ? transformSet.emission() : null;

        return switch (slot) {
            case 0 -> IModelConfig.Factory.ACTIVE_FACTORY.createTransformSet(transform, layer, mask, emission);
            case 1 -> IModelConfig.Factory.ACTIVE_FACTORY.createTransformSet(diffuse, transform, mask, emission);
            case 2 -> IModelConfig.Factory.ACTIVE_FACTORY.createTransformSet(diffuse, layer, transform, emission);
            case 3 -> IModelConfig.Factory.ACTIVE_FACTORY.createTransformSet(diffuse, layer, mask, transform);
            default -> transformSet;
        };
    }

    private static ITransform transformAt(ITransformSet transformSet, int slot) {
        if (transformSet == null) return null;

        return switch (slot) {
            case 0 -> transformSet.diffuse();
            case 1 -> transformSet.layer();
            case 2 -> transformSet.mask();
            case 3 -> transformSet.emission();
            default -> null;
        };
    }

    private void queueVariantRebuild() {
        var canvas = pokeUtilsGui.canvas;
        if (canvas.loadedModel == null || canvas.config == null) return;

        canvas.loadedModel.onUpdate(model -> ModelObjectCompiler.rebuildVariants(model, canvas.config));
        pokeUtilsGui.handler.markDirty();
    }

    private static DragMode dragModeForMouse(Rect rect) {
        float mouseX = ImGui.getIO().getMousePosX();
        float mouseY = ImGui.getIO().getMousePosY();
        if (rect.inResizeHandle(mouseX, mouseY)) return DragMode.RESIZE;
        if (rect.contains(mouseX, mouseY)) return DragMode.MOVE;
        return DragMode.NONE;
    }

    private static Rect transformRect(ITransform transform, ImVec2 imageMin, ImVec2 imageMax) {
        float imageWidth = imageMax.x - imageMin.x;
        float imageHeight = imageMax.y - imageMin.y;
        float u0 = transform.offset().x;
        float v0 = transform.offset().y;
        float u1 = transform.offset().x + transform.scale().x;
        float v1 = transform.offset().y + transform.scale().y;

        float x0 = imageMin.x + u0 * imageWidth;
        float x1 = imageMin.x + u1 * imageWidth;
        float y0 = imageMin.y + (1f - v0) * imageHeight;
        float y1 = imageMin.y + (1f - v1) * imageHeight;

        return new Rect(
                Math.min(x0, x1),
                Math.min(y0, y1),
                Math.max(x0, x1),
                Math.max(y0, y1));
    }

    private static boolean setTransformClamped(ITransform transform, float scaleX, float scaleY, float offsetX, float offsetY) {
        scaleX = clamp(scaleX, MIN_SCALE, 1f);
        scaleY = clamp(scaleY, MIN_SCALE, 1f);
        offsetX = clamp(offsetX, 0f, 1f - scaleX);
        offsetY = clamp(offsetY, 0f, 1f - scaleY);

        Vector2f scale = transform.scale();
        Vector2f offset = transform.offset();
        boolean changed = scale.x != scaleX
                || scale.y != scaleY
                || offset.x != offsetX
                || offset.y != offsetY;

        if (changed) {
            scale.set(scaleX, scaleY);
            offset.set(offsetX, offsetY);
        }

        return changed;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void drawTransformRect(Rect rect, ImVec2 imageMin, ImVec2 imageMax, boolean active) {
        var drawList = ImGui.getWindowDrawList();
        int rectColor = active
                ? ImGui.getColorU32(1f, 0.78f, 0.16f, 1f)
                : ImGui.getColorU32(0.12f, 0.72f, 1f, 1f);
        int handleColor = ImGui.getColorU32(ImGuiCol.Text);

        drawList.pushClipRect(imageMin.x, imageMin.y, imageMax.x, imageMax.y, true);
        drawList.addRect(rect.minX(), rect.minY(), rect.maxX(), rect.maxY(), rectColor, 0f, 0, active ? 3f : 2f);
        drawList.addRectFilled(
                rect.maxX() - HANDLE_SIZE,
                rect.maxY() - HANDLE_SIZE,
                rect.maxX(),
                rect.maxY(),
                handleColor);
        drawList.popClipRect();
    }

    private static ITexture textureAt(ITexture[] images, int index) {
        return index < images.length ? images[index] : null;
    }

    private static String variantName(RareCandyCanvas.BaseMultiRenderObject model, int variantId) {
        return variantId >= 0 && variantId < model.names.variants().size()
                ? model.names.variants().get(variantId)
                : null;
    }

    private static boolean isValidSelection(RareCandyCanvas.BaseMultiRenderObject model, int meshId, int variantId) {
        return meshId >= 0
                && meshId < model.variantRelationships.length
                && variantId >= 0
                && variantId < model.variantRelationships[meshId].length;
    }

    private enum DragMode {
        NONE,
        MOVE,
        RESIZE
    }

    private record EditTarget(Map<String, IVariantDetails> details, boolean defaultVariant) {
    }

    private record Rect(float minX, float minY, float maxX, float maxY) {
        boolean contains(float x, float y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }

        boolean inResizeHandle(float x, float y) {
            return x >= maxX - HANDLE_SIZE && x <= maxX && y >= maxY - HANDLE_SIZE && y <= maxY;
        }
    }
}
