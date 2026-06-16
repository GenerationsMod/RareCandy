package gg.generations.rarecandy.tools.gui;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public final class ViewportGizmos {
    private static final int X_COLOR = ImColor.rgba(218, 63, 52, 255);
    private static final int X_HOVER = ImColor.rgba(255, 106, 94, 255);
    private static final int Y_COLOR = ImColor.rgba(68, 185, 92, 255);
    private static final int Y_HOVER = ImColor.rgba(118, 230, 138, 255);
    private static final int Z_COLOR = ImColor.rgba(65, 126, 226, 255);
    private static final int Z_HOVER = ImColor.rgba(116, 168, 255, 255);
    private static final int WHITE = ImColor.rgba(235, 238, 242, 255);
    private static final int PAD = ImColor.rgba(245, 192, 76, 210);
    private static final int PAD_HOVER = ImColor.rgba(255, 220, 120, 245);
    private static final int RING = ImColor.rgba(120, 170, 255, 210);
    private static final int RING_HOVER = ImColor.rgba(165, 215, 255, 255);
    private static final int PANEL = ImColor.rgba(18, 22, 28, 150);

    private static final float AXIS_PICK_WIDTH = 10.0f;
    private static final float RING_PICK_WIDTH = 8.0f;
    private static final float CENTER_PICK_RADIUS = 14.0f;
    private static final float AXIS_SCREEN_LENGTH = 130.0f;
    private static final float RING_SCREEN_RADIUS = 86.0f;

    private final PokeUtilsGui gui;
    private final Matrix4f viewProjection = new Matrix4f();
    private final Matrix4f inverseViewProjection = new Matrix4f();
    private final Vector3f projectedOrigin = new Vector3f();
    private final Vector3f projectedA = new Vector3f();
    private final Vector3f projectedB = new Vector3f();
    private final Vector3f dragHit = new Vector3f();
    private final Vector3f dragStartTranslation = new Vector3f();
    private final Vector3f dragAxis = new Vector3f();
    private final Vector3f dragGroundOffset = new Vector3f();
    private final Vector3f axis = new Vector3f();
    private final Vector3f viewAxis = new Vector3f();

    private Mode hovered = Mode.NONE;
    private Mode active = Mode.NONE;
    private float handleWorldLength = 1.0f;
    private float ringWorldRadius = 1.0f;
    private float dragStartAxisParam;
    private float dragStartMouseY;
    private float dragStartYaw;
    private float dragStartAngle;
    private float worldUnitsPerPixel = 0.01f;

    public ViewportGizmos(PokeUtilsGui gui) {
        this.gui = gui;
    }

    public void render(boolean enabled) {
        if (!enabled) {
            hovered = Mode.NONE;
            active = Mode.NONE;
            return;
        }

        var canvas = gui.canvas;
        if (canvas == null || RareCandyCanvas.projectionMatrix == null) {
            return;
        }

        ImDrawList drawList = ImGui.getForegroundDrawList();
        renderOrientation(drawList, canvas);

        if (canvas.loadedModelInstance == null) {
            hovered = Mode.NONE;
            active = Mode.NONE;
            return;
        }

        updateMatrices();
        updateHandleScale(canvas);

        if (!project(canvas.modelTranslation, projectedOrigin)) {
            hovered = Mode.NONE;
            active = Mode.NONE;
            return;
        }

        renderManipulator(drawList, canvas);
    }

    public boolean wantsMouseInput() {
        return active != Mode.NONE || hovered != Mode.NONE;
    }

    private void renderOrientation(ImDrawList drawList, RareCandyCanvas canvas) {
        float centerX = Math.max(70.0f, gui.getWidth() - 58.0f);
        float centerY = 72.0f;

        drawList.addRectFilled(centerX - 48.0f, centerY - 48.0f, centerX + 48.0f, centerY + 48.0f, PANEL, 6.0f);
        drawOrientationAxis(drawList, canvas, centerX, centerY, 1.0f, 0.0f, 0.0f, X_COLOR, "X");
        drawOrientationAxis(drawList, canvas, centerX, centerY, 0.0f, 1.0f, 0.0f, Y_COLOR, "Y");
        drawOrientationAxis(drawList, canvas, centerX, centerY, 0.0f, 0.0f, 1.0f, Z_COLOR, "Z");
    }

    private void drawOrientationAxis(ImDrawList drawList, RareCandyCanvas canvas, float centerX, float centerY, float x, float y, float z, int color, String label) {
        axis.set(x, y, z).rotateY(canvas.modelYaw);
        RareCandyCanvas.viewMatrix.transformDirection(axis, viewAxis);

        float endX = centerX + viewAxis.x * 32.0f;
        float endY = centerY - viewAxis.y * 32.0f;
        drawList.addLine(centerX, centerY, endX, endY, color, 2.5f);
        drawList.addCircleFilled(endX, endY, 6.0f, color, 18);
        drawList.addText(endX + 7.0f, endY - 7.0f, WHITE, label);
    }

    private void renderManipulator(ImDrawList drawList, RareCandyCanvas canvas) {
        ImGuiIO io = ImGui.getIO();
        float mouseX = io.getMousePosX();
        float mouseY = io.getMousePosY();

        hovered = pickHandle(canvas, mouseX, mouseY);
        updateActiveHandle(canvas, mouseX, mouseY);

        drawYawRing(drawList, canvas);
        drawAxisHandle(drawList, canvas, Mode.MOVE_X, new Vector3f(1, 0, 0).rotateY(canvas.modelYaw), X_COLOR, X_HOVER, "X");
        drawAxisHandle(drawList, canvas, Mode.MOVE_Y, new Vector3f(0, 1, 0), Y_COLOR, Y_HOVER, "Y");
        drawAxisHandle(drawList, canvas, Mode.MOVE_Z, new Vector3f(0, 0, 1).rotateY(canvas.modelYaw), Z_COLOR, Z_HOVER, "Z");
        drawCenterPad(drawList);
    }

    private Mode pickHandle(RareCandyCanvas canvas, float mouseX, float mouseY) {
        if (distance(mouseX, mouseY, projectedOrigin.x, projectedOrigin.y) <= CENTER_PICK_RADIUS) {
            return Mode.MOVE_XZ;
        }

        Mode pickedAxis = pickAxis(canvas, mouseX, mouseY);
        if (pickedAxis != Mode.NONE) {
            return pickedAxis;
        }

        return pickYawRing(canvas, mouseX, mouseY) ? Mode.ROTATE_Y : Mode.NONE;
    }

    private Mode pickAxis(RareCandyCanvas canvas, float mouseX, float mouseY) {
        if (axisDistance(canvas, mouseX, mouseY, new Vector3f(1, 0, 0).rotateY(canvas.modelYaw)) <= AXIS_PICK_WIDTH) {
            return Mode.MOVE_X;
        }
        if (axisDistance(canvas, mouseX, mouseY, new Vector3f(0, 1, 0)) <= AXIS_PICK_WIDTH) {
            return Mode.MOVE_Y;
        }
        if (axisDistance(canvas, mouseX, mouseY, new Vector3f(0, 0, 1).rotateY(canvas.modelYaw)) <= AXIS_PICK_WIDTH) {
            return Mode.MOVE_Z;
        }
        return Mode.NONE;
    }

    private float axisDistance(RareCandyCanvas canvas, float mouseX, float mouseY, Vector3f direction) {
        if (!project(axisEnd(canvas.modelTranslation, direction, handleWorldLength), projectedA)) {
            return Float.MAX_VALUE;
        }
        return distanceToSegment(mouseX, mouseY, projectedOrigin.x, projectedOrigin.y, projectedA.x, projectedA.y);
    }

    private boolean pickYawRing(RareCandyCanvas canvas, float mouseX, float mouseY) {
        float best = Float.MAX_VALUE;
        for (int i = 0; i < 80; i++) {
            float a0 = (float) (i * Math.PI * 2.0 / 80.0);
            float a1 = (float) ((i + 1) * Math.PI * 2.0 / 80.0);
            if (project(ringPoint(canvas.modelTranslation, a0), projectedA) && project(ringPoint(canvas.modelTranslation, a1), projectedB)) {
                best = Math.min(best, distanceToSegment(mouseX, mouseY, projectedA.x, projectedA.y, projectedB.x, projectedB.y));
            }
        }
        return best <= RING_PICK_WIDTH;
    }

    private void updateActiveHandle(RareCandyCanvas canvas, float mouseX, float mouseY) {
        if (active == Mode.NONE && ImGui.isMouseClicked(GLFW_MOUSE_BUTTON_LEFT) && hovered != Mode.NONE) {
            active = hovered;
            dragStartTranslation.set(canvas.modelTranslation);

            if (active == Mode.MOVE_X || active == Mode.MOVE_Z) {
                dragAxis.set(active == Mode.MOVE_X ? 1.0f : 0.0f, 0.0f, active == Mode.MOVE_Z ? 1.0f : 0.0f).rotateY(canvas.modelYaw);
                intersectGround(mouseX, mouseY, canvas.modelTranslation.y, dragHit);
                dragStartAxisParam = dragHit.sub(canvas.modelTranslation, new Vector3f()).dot(dragAxis);
            } else if (active == Mode.MOVE_Y) {
                dragStartMouseY = mouseY;
            } else if (active == Mode.MOVE_XZ && intersectGround(mouseX, mouseY, canvas.modelTranslation.y, dragHit)) {
                dragGroundOffset.set(canvas.modelTranslation).sub(dragHit);
            } else if (active == Mode.ROTATE_Y && intersectGround(mouseX, mouseY, canvas.modelTranslation.y, dragHit)) {
                dragStartYaw = canvas.modelYaw;
                dragStartAngle = groundAngle(canvas.modelTranslation, dragHit);
            }
        }

        if (active == Mode.NONE) {
            return;
        }

        if (!ImGui.isMouseDown(GLFW_MOUSE_BUTTON_LEFT)) {
            active = Mode.NONE;
            return;
        }

        switch (active) {
            case MOVE_X, MOVE_Z -> updateAxisDrag(canvas, mouseX, mouseY);
            case MOVE_Y -> canvas.modelTranslation.y = dragStartTranslation.y - (mouseY - dragStartMouseY) * worldUnitsPerPixel;
            case MOVE_XZ -> {
                if (intersectGround(mouseX, mouseY, dragStartTranslation.y, dragHit)) {
                    canvas.modelTranslation.set(dragHit).add(dragGroundOffset);
                }
            }
            case ROTATE_Y -> {
                if (intersectGround(mouseX, mouseY, canvas.modelTranslation.y, dragHit)) {
                    canvas.modelYaw = dragStartYaw + groundAngle(canvas.modelTranslation, dragHit) - dragStartAngle;
                }
            }
        }
    }

    private void updateAxisDrag(RareCandyCanvas canvas, float mouseX, float mouseY) {
        if (!intersectGround(mouseX, mouseY, dragStartTranslation.y, dragHit)) {
            return;
        }

        float param = dragHit.sub(dragStartTranslation, new Vector3f()).dot(dragAxis);
        canvas.modelTranslation.set(dragStartTranslation).fma(param - dragStartAxisParam, dragAxis);
    }

    private void drawAxisHandle(ImDrawList drawList, RareCandyCanvas canvas, Mode mode, Vector3f direction, int color, int hoverColor, String label) {
        Vector3f end = axisEnd(canvas.modelTranslation, direction, handleWorldLength);
        if (!project(end, projectedA)) {
            return;
        }

        int drawColor = hovered == mode || active == mode ? hoverColor : color;
        drawList.addLine(projectedOrigin.x, projectedOrigin.y, projectedA.x, projectedA.y, drawColor, 4.0f);
        drawList.addCircleFilled(projectedA.x, projectedA.y, 7.0f, drawColor, 20);
        drawList.addText(projectedA.x + 9.0f, projectedA.y - 9.0f, WHITE, label);
    }

    private void drawCenterPad(ImDrawList drawList) {
        int color = hovered == Mode.MOVE_XZ || active == Mode.MOVE_XZ ? PAD_HOVER : PAD;
        float x = projectedOrigin.x;
        float y = projectedOrigin.y;
        drawList.addQuadFilled(x, y - 13.0f, x + 13.0f, y, x, y + 13.0f, x - 13.0f, y, color);
        drawList.addQuad(x, y - 13.0f, x + 13.0f, y, x, y + 13.0f, x - 13.0f, y, WHITE, 1.5f);
    }

    private void drawYawRing(ImDrawList drawList, RareCandyCanvas canvas) {
        int color = hovered == Mode.ROTATE_Y || active == Mode.ROTATE_Y ? RING_HOVER : RING;
        boolean hasPrevious = false;
        float previousX = 0.0f;
        float previousY = 0.0f;

        for (int i = 0; i <= 96; i++) {
            float angle = (float) (i * Math.PI * 2.0 / 96.0);
            if (project(ringPoint(canvas.modelTranslation, angle), projectedA)) {
                if (hasPrevious) {
                    drawList.addLine(previousX, previousY, projectedA.x, projectedA.y, color, 3.0f);
                }
                previousX = projectedA.x;
                previousY = projectedA.y;
                hasPrevious = true;
            } else {
                hasPrevious = false;
            }
        }
    }

    private void updateMatrices() {
        viewProjection.set(RareCandyCanvas.projectionMatrix).mul(RareCandyCanvas.viewMatrix);
        inverseViewProjection.set(viewProjection).invert();
    }

    private void updateHandleScale(RareCandyCanvas canvas) {
        Vector4f viewPos = RareCandyCanvas.viewMatrix.transform(new Vector4f(canvas.modelTranslation, 1.0f));
        float depth = Math.max(0.1f, Math.abs(viewPos.z));
        float projectionScaleY = Math.max(0.0001f, RareCandyCanvas.projectionMatrix.m11());
        worldUnitsPerPixel = depth / (projectionScaleY * Math.max(1.0f, gui.getHeight()) * 0.5f);
        handleWorldLength = Math.max(0.05f, worldUnitsPerPixel * AXIS_SCREEN_LENGTH);
        ringWorldRadius = Math.max(0.05f, worldUnitsPerPixel * RING_SCREEN_RADIUS);
    }

    private boolean project(Vector3f world, Vector3f screen) {
        Vector4f projected = viewProjection.transform(new Vector4f(world, 1.0f));
        if (projected.w <= 0.0f) {
            return false;
        }

        projected.div(projected.w);
        screen.set(
                (projected.x * 0.5f + 0.5f) * gui.getWidth(),
                (1.0f - (projected.y * 0.5f + 0.5f)) * gui.getHeight(),
                projected.z
        );
        return projected.z >= -1.0f && projected.z <= 1.0f;
    }

    private boolean intersectGround(float mouseX, float mouseY, float groundY, Vector3f out) {
        float ndcX = (mouseX / Math.max(1.0f, gui.getWidth())) * 2.0f - 1.0f;
        float ndcY = 1.0f - (mouseY / Math.max(1.0f, gui.getHeight())) * 2.0f;

        Vector4f near = inverseViewProjection.transform(new Vector4f(ndcX, ndcY, -1.0f, 1.0f));
        Vector4f far = inverseViewProjection.transform(new Vector4f(ndcX, ndcY, 1.0f, 1.0f));
        near.div(near.w);
        far.div(far.w);

        float rayY = far.y - near.y;
        if (Math.abs(rayY) < 0.000001f) {
            return false;
        }

        float t = (groundY - near.y) / rayY;
        if (t < 0.0f) {
            return false;
        }

        out.set(
                near.x + (far.x - near.x) * t,
                groundY,
                near.z + (far.z - near.z) * t
        );
        return true;
    }

    private Vector3f axisEnd(Vector3f origin, Vector3f direction, float length) {
        return new Vector3f(origin).fma(length, direction);
    }

    private Vector3f ringPoint(Vector3f origin, float angle) {
        return new Vector3f(
                origin.x + (float) Math.cos(angle) * ringWorldRadius,
                origin.y,
                origin.z + (float) Math.sin(angle) * ringWorldRadius
        );
    }

    private static float groundAngle(Vector3f origin, Vector3f point) {
        return (float) Math.atan2(point.z - origin.z, point.x - origin.x);
    }

    private static float distance(float x0, float y0, float x1, float y1) {
        float dx = x0 - x1;
        float dy = y0 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float distanceToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float lenSq = dx * dx + dy * dy;
        if (lenSq <= 0.000001f) {
            return distance(px, py, ax, ay);
        }

        float t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
        t = Math.max(0.0f, Math.min(1.0f, t));
        return distance(px, py, ax + dx * t, ay + dy * t);
    }

    private enum Mode {
        NONE,
        MOVE_X,
        MOVE_Y,
        MOVE_Z,
        MOVE_XZ,
        ROTATE_Y
    }
}
