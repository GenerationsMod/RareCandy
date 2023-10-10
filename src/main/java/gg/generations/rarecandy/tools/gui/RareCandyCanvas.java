package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.arceus.core.DefaultRenderGraph;
import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.tools.util.SimpleRenderingInstance;
import gg.generationsmod.rarecandy.model.Model;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RareCandyCanvas extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f();
    public double startTime = System.currentTimeMillis();
    public String currentAnimation = null;
    private RareCandyScene<RenderingInstance> scene = new RareCandyScene<>();
    private DefaultRenderGraph graph = new DefaultRenderGraph(scene);
    private RenderingInstance displayModel;

    public RareCandyCanvas() {
        super(defaultData());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                RareCandyCanvas.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
            }
        });
    }

    private static GLData defaultData() {
        var data = new GLData();
        data.profile = GLData.Profile.CORE;
        data.forwardCompatible = true;
        data.api = GLData.API.GL;
        data.majorVersion = 3;
        data.minorVersion = 2;
        return data;
    }

    public void openFile(Model model) {
        currentAnimation = null;
        scene.removeInstance(displayModel);
        this.displayModel = new SimpleRenderingInstance(load(model));
        scene.addInstance(displayModel);
    }

    private gg.generations.rarecandy.arceus.model.Model load(Model model) {
        throw new RuntimeException("Fix");
    }

    @Override
    public void initGL() {
        RareCandyCanvas.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 100.0f);
        GL.createCapabilities(true);
        GL11C.glClearColor(60 / 255f, 63 / 255f, 65 / 255f, 1);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
    }

    @Override
    public void paintGL() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        graph.render();
        swapBuffers();

//        if (instances.size() > 1) {
//            ((MultiRenderObject<AnimatedMeshObject>) instances.get(0).object()).onUpdate(a -> {
//                for (var instance : instances) {
//                    if(a.animations != null) {
//                        var newAnimation = a.animations.get(currentAnimation);
//
//                        if(newAnimation != null) {
//                            instance.changeAnimation(new AnimationInstance(newAnimation));
//                        }
//                    }
//                }
//            });
//        }
    }

    public void setAnimation(@NotNull String animation) {
//        var object = loadedModel.objects.get(0);
//
//        if (object.animations != null)
//            LoggerUtil.print(animation);
//        if (Objects.requireNonNull(object.animations).containsKey(animation)) {
//            loadedModelInstance.changeAnimation(new AnimationInstance(object.animations.get(animation)));
//        }
    }

    public void setVariant(String variant) {
//        loadedModelInstance.setVariant(variant);
    }

    public void attachArcBall() {
        var arcballOrbit = new ArcBallOrbit(viewMatrix, 3f, 0.125f, 0f);
        this.addMouseMotionListener(arcballOrbit);
        this.addMouseWheelListener(arcballOrbit);
        this.addMouseListener(arcballOrbit);
    }

    public static class ArcBallOrbit implements MouseMotionListener, MouseWheelListener, MouseListener {
        private final Matrix4f viewMatrix;
        private float radius;
        private float angleX;
        private float angleY;
        private int lastX, lastY;
        private float offsetX, offsetY;

        public ArcBallOrbit(Matrix4f viewMatrix, float radius, float angleX, float angleY) {
            this.viewMatrix = viewMatrix;
            this.radius = radius;
            this.angleX = angleX;
            this.angleY = angleY;
            update();
        }

        public void update() {
            viewMatrix.identity().arcball(radius, 0, 0, 0, (angleY + offsetY) * (float) Math.PI * 2f, (angleX + offsetX) * (float) Math.PI * 2f);
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
    }
}