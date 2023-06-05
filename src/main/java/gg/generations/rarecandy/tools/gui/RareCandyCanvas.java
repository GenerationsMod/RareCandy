package gg.generations.rarecandy.tools.gui;

import gg.generations.pokeutils.GlbPixelAsset;
import gg.generations.pokeutils.PixelAsset;
import gg.generations.rarecandy.animation.AnimationInstance;
import gg.generations.rarecandy.components.AnimatedMeshObject;
import gg.generations.rarecandy.components.MeshObject;
import gg.generations.rarecandy.components.MultiRenderObject;
import gg.generations.rarecandy.loading.ModelLoader;
import gg.generations.rarecandy.pipeline.Pipeline;
import gg.generations.rarecandy.rendering.ObjectInstance;
import gg.generations.rarecandy.rendering.RareCandy;
import gg.generations.rarecandy.storage.AnimatedObjectInstance;
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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static gg.generations.rarecandy.tools.gui.GuiPipelines.STATIC;


public class RareCandyCanvas extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f();
    public double startTime = System.currentTimeMillis();
    private RareCandy renderer;
    public final List<AnimatedObjectInstance> instances = new ArrayList<>();
    private int scaleModifier = 0;
    public String currentAnimation = null;

    private MultiRenderObject<MeshObject> plane;

    private MultiRenderObject<AnimatedMeshObject> loadedModel;
    private AnimatedObjectInstance loadedModelInstance;


    public RareCandyCanvas() {
        super(defaultData());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
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

    public void openFile(PixelAsset pkFile) {
        currentAnimation = null;
        renderer.objectManager.clearObjects();
        renderer.objectManager.add(plane, new ObjectInstance(new Matrix4f(), viewMatrix, null));
        loadPokemonModel(renderer, pkFile, model -> {
            var i = 0;

            loadedModel = model;
            var variants = model.availableVariants();

            var variant = !variants.isEmpty() ? variants.iterator().next() : null;
            var instance = new AnimatedObjectInstance(new Matrix4f(), viewMatrix, variant);
            instance.transformationMatrix().scale(0.3f);
            loadedModelInstance = renderer.objectManager.add(model, instance);

            model.updateDimensions();
        });
    }

    @Override
    public void initGL() {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize();
        this.renderer = new RareCandy();

        GL11C.glClearColor(60 / 255f, 63 / 255f, 65 / 255f, 1);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        try(var is = Pipeline.class.getResourceAsStream("/models/grid.glb")) {
            assert is != null;
            load(renderer, new GlbPixelAsset("plane", is.readAllBytes()), s -> STATIC, model -> {
                plane = model;
                renderer.objectManager.add(model, new ObjectInstance(new Matrix4f(), viewMatrix, null));
            }, MeshObject::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paintGL() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        renderer.render(false, ((System.currentTimeMillis() - startTime) / 16000));
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
//
//        for (var instance : instances) {
//            if (scaleModifier != 0) {
//                var newScale = 1 - (scaleModifier * 0.1f);
//                instance.transformationMatrix().scale(newScale);
//            }
//        }
    }

    protected void loadPokemonModel(RareCandy renderer, PixelAsset is, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        load(renderer, is, this::getPokemonPipeline, onFinish, AnimatedMeshObject::new);
    }

    protected <T extends MeshObject> void load(RareCandy renderer, PixelAsset is, Function<String, Pipeline> pipelineFactory, Consumer<MultiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        loader.createObject(
                () -> is,
                (gltfModel, smdFileMap, gfbFileMap, images, config, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    ModelLoader.create2(object, gltfModel, smdFileMap, gfbFileMap, images, config, glCalls, pipelineFactory, supplier);
                    return glCalls;
                },
                onFinish
        );
    }

    private Pipeline getPokemonPipeline(String materialName) {
        if(materialName.equals("transparent")) {
            return GuiPipelines.TRANSPARENT;
        } else {
            return GuiPipelines.ANIMATED;
        }
    }

    public void setAnimation(@NotNull String animation) {
        var object = loadedModel.objects.get(0);

        if (object.animations != null)
            System.out.println(animation);
        if (Objects.requireNonNull(object.animations).containsKey(animation)) {
            loadedModelInstance.changeAnimation(new AnimationInstance(object.animations.get(animation)));
        }
    }

    public void updateLoadedModel(Consumer<AnimatedMeshObject> consumer) {
        loadedModel.onUpdate(consumer);
    }

    public void setVariant(String variant) {
        loadedModelInstance.setVariant(variant);
    }

    public void attachArcBall() {
        var arcballOrbit = new ArcballOrbit(viewMatrix, 3f, 0.125f, 0f);
        this.addMouseMotionListener(arcballOrbit);
        this.addMouseWheelListener(arcballOrbit);
        this.addMouseListener(arcballOrbit);
    }

    public static class ArcballOrbit implements MouseMotionListener, MouseWheelListener, MouseListener {
        private final Matrix4f viewMatrix;
        private float radius;
        private float angleX;
        private float angleY;
        private int lastX, lastY;
        private float offsetX, offsetY;

        public ArcballOrbit(Matrix4f viewMatrix, float radius, float angleX, float angleY) {
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
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    }
}