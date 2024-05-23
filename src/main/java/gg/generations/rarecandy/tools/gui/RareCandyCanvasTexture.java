package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.*;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.TextureDisplayObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.tools.TextureLoader;
import gg.generations.rarecandy.tools.gui.GuiPipelines;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;


public class RareCandyCanvasTexture extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;

    private final ModelLoader loader = new ModelLoader();

    private static final float lightLevel = 1f;

    public final Matrix4f viewMatrix = new Matrix4f();
    private RareCandy renderer;
    private MultiRenderObject<TextureDisplayObject> plane;
    private Map<String, Material> materials;
    private ObjectInstance instance;

    public RareCandyCanvasTexture() {
        super(defaultData());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
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

    public static void setup(RareCandyCanvasTexture canvas) {
        canvas.attachArcBall();

        ITextureLoader.setInstance(new TextureLoader());

        PipelineRegistry.setFunction(s-> switch(s) {
            case "masked" -> GuiPipelines.MASKED;
            case "layered" -> GuiPipelines.LAYERED;
            case "paradox" -> GuiPipelines.PARADOX;
            case "plane" -> GuiPipelines.PLANE;
            default -> GuiPipelines.SOLID;
        });

        var renderLoop = new Runnable() {
            @Override
            public void run() {
                if (canvas.isValid()) canvas.render();
                SwingUtilities.invokeLater(this);
            }
        };

        SwingUtilities.invokeLater(renderLoop);
    }

    public void openFile(PixelAsset pkFile) throws IOException {
        if(pkFile == null) return;

        loadPokemonModel(pkFile, model -> {
            plane.objects.get(0).setMaterials(materials);
        });
    }

    @Override
    public void initGL() {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize();
        this.renderer = new RareCandy();

        GL11C.glClearColor(0, 0, 0, 1);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        loadPlane(2, 2, model -> {
            plane = model;
            instance = new ObjectInstance(new Matrix4f(), viewMatrix, null);
            renderer.objectManager.add(model, instance);
        });
    }

    private MultiRenderObject<TextureDisplayObject> loadPlane(int width, int length, Consumer<MultiRenderObject<TextureDisplayObject>> onFinish) {
        return loader.generatePlaneDisplay(width, length, onFinish);
    }

    private final Vector3f size = new Vector3f();

    private final double fraciton = 1/16f;
    @Override
    public void paintGL() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

        renderer.render(false, 0f);
        swapBuffers();
    }

    public AnimationInstance createInstance(Animation animation) {
        return new AnimationInstance(animation);
    }

    protected void loadPokemonModel(PixelAsset is, Consumer<MultiRenderObject<TextureDisplayObject>> onFinish) {
        load(is, onFinish);
    }

    protected void load(PixelAsset is, Consumer<MultiRenderObject<TextureDisplayObject>> onFinish) {
        loader.createObject(
                () -> is,
                (gltfModel, smdFileMap, gfbFileMap, tramnAnimations, images, config, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    materials = ModelLoader.getMaterials(images, config);
                    return glCalls;
                },
                onFinish
        );
    }

    public void attachArcBall() {
        var arcballOrbit = new ArcballOrbit(viewMatrix, 3f, 0.125f, 0f);
        this.addMouseMotionListener(arcballOrbit);
        this.addMouseWheelListener(arcballOrbit);
        this.addMouseListener(arcballOrbit);
        this.addKeyListener(arcballOrbit);
    }

    public void setVariant(String string) {
        instance.setVariant(string);
    }

    public static class ArcballOrbit implements MouseMotionListener, MouseWheelListener, MouseListener, KeyListener {
        private final Matrix4f viewMatrix;
        private float radius;
        private float angleX;
        private float angleY;
        private int lastX, lastY;
        private float offsetX, offsetY;

        private final Vector3f centerOffset = new Vector3f();

        public ArcballOrbit(Matrix4f viewMatrix, float radius, float angleX, float angleY) {
            this.viewMatrix = viewMatrix;
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

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            float lateralStep = 0.001f; // Adjust the step size as needed

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> centerOffset.x -= lateralStep;
                case KeyEvent.VK_RIGHT -> centerOffset.x += lateralStep;
                case KeyEvent.VK_UP -> centerOffset.z += lateralStep;
                case KeyEvent.VK_DOWN -> centerOffset.z -= lateralStep;
                case KeyEvent.VK_PAGE_UP -> centerOffset.y += lateralStep;
                case KeyEvent.VK_PAGE_DOWN -> centerOffset.y -= lateralStep;
            }

            update();
        }
        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}

