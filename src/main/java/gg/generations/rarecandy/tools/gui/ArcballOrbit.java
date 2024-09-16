package gg.generations.rarecandy.tools.gui;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.event.*;

public class ArcballOrbit implements MouseMotionListener, MouseWheelListener, MouseListener {
    private final Matrix4f viewMatrix;
    private final RareCandyCanvas canvas;
    private float radius;
    private float angleX;
    private float angleY;
    private float lastX, lastY, offsetX, offsetY;

    private final Vector3f centerOffset = new Vector3f();

    public ArcballOrbit(RareCandyCanvas canvas, float radius, float angleX, float angleY) {
        this.viewMatrix = canvas.viewMatrix;
        this.canvas = canvas;
        this.radius = radius;
        this.angleX = angleX;
        this.angleY = angleY;
        update();
    }

    public void update() {
        viewMatrix.identity().arcball(radius, centerOffset.x, centerOffset.y, centerOffset.z, (angleY + offsetY) * (float) Math.PI * 2f, (angleX + offsetX) * (float) Math.PI * 2f);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        offsetX = (x - lastX) * 0.001f;
        offsetY = (y - lastY) * 0.001f;
        update();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int scrollAmount = e.getWheelRotation();
        radius += scrollAmount * 0.1f;
        update();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        offsetX = 0;
        offsetY = 0;

        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        angleX += offsetX;
        angleY += offsetY;
        offsetX = 0;
        offsetY = 0;
        lastX = 0;
        lastY = 0;

        update();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }


    public void keyPressed(int code) {
        float lateralStep = 0.01f; // Adjust the step size as needed

        if (!RareCandyCanvas.cycling) {

            switch (code) {
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> centerOffset.x -= lateralStep;
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> centerOffset.x += lateralStep;
                case KeyEvent.VK_UP, KeyEvent.VK_W -> centerOffset.z += lateralStep;
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> centerOffset.z -= lateralStep;
                case KeyEvent.VK_PAGE_UP, KeyEvent.VK_Q -> centerOffset.y += lateralStep;
                case KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_E -> centerOffset.y -= lateralStep;
            }

            update();
        }
    }

    public void reset() {
        if (canvas.loadedModel == null) {
            radius = 2f;
            centerOffset.set(0, 0, 0);
        } else {
            radius = ((canvas.loadedModel.dimensions.get(canvas.loadedModel.dimensions.maxComponent())) * canvas.loadedModel.scale) / 2f;
            centerOffset.set(0, radius, 0);
        }

        lastX = lastY = 0;
        angleX = -0.125f;
        angleY = 0.125f;
    }

}
