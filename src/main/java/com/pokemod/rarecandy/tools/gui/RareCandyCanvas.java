package com.pokemod.rarecandy.tools.gui;

import com.pokemod.pokeutils.GlbPixelAsset;
import com.pokemod.pokeutils.PixelAsset;
import com.pokemod.pokeutils.tranm.Vec3f;
import com.pokemod.rarecandy.animation.Animation;
import com.pokemod.rarecandy.animation.AnimationInstance;
import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.components.SkyboxRenderObject;
import com.pokemod.rarecandy.loading.CubeMapTexture;
import com.pokemod.rarecandy.loading.ModelLoader;
import com.pokemod.rarecandy.loading.Texture;
import com.pokemod.rarecandy.pipeline.ShaderPipeline;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.rarecandy.storage.AnimatedObjectInstance;
import com.pokemod.rarecandy.tools.pixelmonTester.PokemonTest;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

import static java.lang.Math.cos;
import static java.lang.Math.sin;

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
    private GuiPipelines pipelines;
    public CubeMapTexture cubeMap;
    public CubeMapTexture terastallizeCubeMap;
    public Texture starsTexture;
    public Texture normalMap;

    private PixelAsset pkFile;
    private GuiPipelines.LightingType lightingType = GuiPipelines.LightingType.GLOSSY_EXPERIMENTAL;
    private SkyboxRenderObject skybox;

    public RareCandyCanvas() {
        super(defaultData());
        Animation.animationModifier = (animation, s) -> animation.ticksPerSecond = 16;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
            }
        });

//        this.cubeMap = skybox.texture;
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
        this.pkFile = pkFile;
        renderer.objectManager.clearObjects();
        renderer.objectManager.add(plane, new ObjectInstance(new Matrix4f(), viewMatrix, null));
//        renderer.objectManager.add(skybox, new ObjectInstance(new Matrix4f(), viewMatrix, ""));
        loadPokemonModel(renderer, pkFile, model -> {
            var i = 0;

            loadedModel = model;
            var variant = model.objects.get(0).availableVariants().iterator().next();
            var instance = new AnimatedObjectInstance(new Matrix4f(), viewMatrix, variant);
            instance.transformationMatrix().scale(0.3f);
            loadedModelInstance = renderer.objectManager.add(model, instance);

            model.updateDimensions();
        });
    }

    @Override
    public void initGL() {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);

        try {
            this.renderer = new RareCandy();
            this.pipelines = new GuiPipelines(() -> projectionMatrix, this);
            skybox = new SkyboxRenderObject(pipelines.skybox);
//            renderer.objectManager.add(skybox, new ObjectInstance(new Matrix4f(), viewMatrix, ""));
            var skybox = new SkyboxRenderObject(pipelines.skybox);
            cubeMap = skybox.texture;
            this.terastallizeCubeMap = new CubeMapTexture("D:\\Git Repos\\RareCandy\\src\\renderer\\resources\\cubemap\\terastallize\\panorama_");
            this.starsTexture = new Texture(PokemonTest.class.getResourceAsStream("/shared/stars.png").readAllBytes(), "stars.png");
            this.normalMap = new Texture(PokemonTest.class.getResourceAsStream("/shared/normalMap.png").readAllBytes(), "normalMap.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GL11C.glClearColor(60 / 255f, 63 / 255f, 65 / 255f, 1);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        try(var is = ShaderPipeline.class.getResourceAsStream("/models/grid.glb")) {
            assert is != null;
            load(renderer, new GlbPixelAsset("plane", is.readAllBytes()), s -> pipelines.cachePipeline(GuiPipelines.LightingType.BASIC_FAST, false), model -> {
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
        renderer.render(false, ((System.currentTimeMillis() - startTime)/200));
        swapBuffers();
    }

    protected void loadPokemonModel(RareCandy renderer, PixelAsset is, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        load(renderer, is, this::getPokemonPipeline, onFinish, AnimatedMeshObject::new);
    }

    protected <T extends MeshObject> void load(RareCandy renderer, PixelAsset is, Function<String, ShaderPipeline> pipelineFactory, Consumer<MultiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        loader.createObject(
                () -> is,
                (gltfModel, smdFileMap, gfbFileMap, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    ModelLoader.create2(object, gltfModel, smdFileMap, gfbFileMap, glCalls, pipelineFactory, supplier);
                    return glCalls;
                },
                onFinish
        );
    }

    private ShaderPipeline getPokemonPipeline(String type) {
        return pipelines.cachePipeline(lightingType, true);
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
            var theta = (angleX + offsetX) * (float) Math.PI * 2f;
            var phi = (angleY + offsetY) * (float) Math.PI * 2f;

            viewMatrix.identity().arcball(radius, 0, 0, 0, phi, theta);

            float x = (float) (radius * sin(theta) * cos(phi));
            float y = (float) (radius * sin(theta) * sin(phi));
            float z = (float) (radius * cos(theta));

            UniformSettings.camPos.set(x,y,z);
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

    public static class UniformSettings {
        public static float metallic = 0.5f;
        public static float roughness = 0.4f;
        public static float ao = 1.0f;
        public static Vector3f lightPosition = new Vector3f(0, 2, 0);
        public static float reflectivity = 3f;
        public static float shineDamper = 2.5f;
        public static int intColor = 0xFFFFFF;
        public static float diffuseColorMix = 0.5f;
        public static float underlyingTexCoordMix = 0.6f;
        public static Vector3f camPos = new Vector3f(0, 0, -1);
    }
}