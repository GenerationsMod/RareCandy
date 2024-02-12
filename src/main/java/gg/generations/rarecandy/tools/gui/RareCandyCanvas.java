package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.arceus.core.DefaultRenderGraph;
import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.pk.PipelineRegistry;
import gg.generations.rarecandy.arceus.model.pk.TextureLoader;
import gg.generationsmod.rarecandy.assimp.AssimpModelLoader;
import gg.generationsmod.rarecandy.model.RawModel;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gg.generations.rarecandy.tools.gui.PlaneGenerator.generatePlane;

public class RareCandyCanvas extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f();
    public double startTime = System.currentTimeMillis();
    public String currentAnimation = null;
    private RareCandyScene<RenderingInstance> scene = new RareCandyScene<>();
    private DefaultRenderGraph graph = new DefaultRenderGraph(scene);
    private MultiRenderObject.MultiRenderObjectInstance displayModel;
    private List<Runnable> runnables = new ArrayList<>();

    public RareCandyCanvas() {
        super(defaultData());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                RareCandyCanvas.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
            }
        });

        AssimpModelLoader.setImageConsumer((s, image)-> TextureLoader.instance().register(s, image));
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

    public void openFile(RawModel rawModel) {
        runnables.add(() -> {
            currentAnimation = null;
//            if (displayModel != null) displayModel.remove(scene);
            this.displayModel = new MultiRenderObject.MultiRenderObjectInstance(new MultiRenderObject<RenderingInstance>(rawModel), new Matrix4f().scale(rawModel.config().scale), null);
            displayModel.addToScene(scene);
        });
    }

    @Override
    public void initGL() {
        RareCandyCanvas.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 100.0f);
        GL.createCapabilities(true);

        PipelineRegistry.setFunction(GuiPipelines.of(() -> projectionMatrix, viewMatrix));

        GL11C.glClearColor(255 / 255f, 255 / 255f, 255 / 255f, 1);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        try {
            scene.addInstance(generatePlane(projectionMatrix, viewMatrix, 10, 10));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paintGL() {
        runnables.forEach(Runnable::run);
        runnables.clear();

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