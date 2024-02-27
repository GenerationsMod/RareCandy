package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.arceus.core.DefaultRenderGraph;
import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.pk.MultiRenderObject;
import gg.generations.rarecandy.arceus.model.pk.MultiRenderObjectInstance;
import gg.generations.rarecandy.arceus.model.pk.PipelineRegistry;
import gg.generations.rarecandy.arceus.model.pk.TextureLoader;
import gg.generations.rarecandy.legacy.LoggerUtil;
import gg.generations.rarecandy.legacy.animation.AnimationController;
import gg.generations.rarecandy.legacy.animation.AnimationInstance;
import gg.generationsmod.rarecandy.assimp.AssimpModelLoader;
import gg.generationsmod.rarecandy.model.RawModel;
import gg.generationsmod.rarecandy.model.animation.Animation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gg.generations.rarecandy.tools.gui.PlaneGenerator.generatePlane;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL43C.*;

public class RareCandyCanvas extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f();
    public double startTime = System.currentTimeMillis();
    public double time;

    public AnimationController animationController = new AnimationController();

    public String currentAnimation = null;
    private RareCandyScene<RenderingInstance> scene = new RareCandyScene<>();
    private DefaultRenderGraph graph = new DefaultRenderGraph(scene);
    private MultiRenderObjectInstance displayModel;
    private List<Runnable> runnables = new ArrayList<>();
    private Callback debugCallbackKeepAroundAlways;

    public RareCandyCanvas() {
        super(defaultData());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                RareCandyCanvas.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
            }
        });
    }

    public static void error(
            @NativeType("GLenum") int source,
            @NativeType("GLenum") int type,
            @NativeType("GLuint") int id,
            @NativeType("GLenum") int severity,
            @NativeType("GLsizei") int length,
            @NativeType("GLchar const *") long message,
            @NativeType("void const *") long userParam) {
        var stream = type == GL_DEBUG_TYPE_ERROR ? System.err : System.out;

        stream.printf("GL CALLBACK: %s Severity = %s Message = %s\n", type == GL_DEBUG_TYPE_ERROR ? "ERROR" : "OTHER", severity, MemoryUtil.memUTF8(message));
        if(type == GL_DEBUG_TYPE_ERROR) System.exit(-2);
    }

    private static GLData defaultData() {
        var data = new GLData();
        data.profile = GLData.Profile.CORE;
        data.forwardCompatible = true;
        data.api = GLData.API.GL;
        data.majorVersion = 4;
        data.minorVersion = 5;
        return data;
    }

    public void openFile(RawModel rawModel) {
        runnables.add(() -> {
            currentAnimation = null;
            if (displayModel != null) displayModel.removeFromScene();
            this.displayModel = new MultiRenderObjectInstance(new MultiRenderObject<RenderingInstance>(rawModel), new Matrix4f());
            displayModel.addToScene(scene);
        });
    }

    @Override
    public void initGL() {
        RareCandyCanvas.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 100.0f);
        GL.createCapabilities(true);

        AssimpModelLoader.setImageConsumer((s, image) -> TextureLoader.instance().register(s, image));

        PipelineRegistry.setFunction(GuiPipelines.of(() -> projectionMatrix, viewMatrix, this));

        GL11C.glClearColor(255 / 255f, 255 / 255f, 255 / 255f, 1);
        glEnable(GL11C.GL_DEPTH_TEST);

        glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        this.debugCallbackKeepAroundAlways = GLUtil.setupDebugMessageCallback();

        try {
            scene.addInstance(generatePlane(projectionMatrix, viewMatrix, 6, 6));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void paintGL() {
//        checkError();
        runnables.forEach(Runnable::run);
        runnables.clear();
//        checkError();
        time = (System.currentTimeMillis() - startTime) / 1000f;

        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        animationController.render(time);
        graph.render();
//        checkError();
        swapBuffers();
    }

    public void setAnimation(@NotNull String animation) {
        Map<String, Animation<?>> animations = displayModel.getAnimationsIfAvailable();

        LoggerUtil.print(animation);
        if (animations.containsKey(animation)) {
            var instance = new AnimationInstance(animations.get(animation));
            animationController.playingInstances.add(instance);
            displayModel.changeAnimation(instance);
        }
    }

    public void setVariant(String variant) {
        displayModel.setVariant(variant);
    }

    public void attachArcBall() {
        var arcballOrbit = new ArcBallOrbit(viewMatrix, 6f, 0.125f, 0.125f);
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