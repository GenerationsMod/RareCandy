package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.LoggerUtil;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.neo.regular.Pipeline;
import gg.generations.rarecandy.renderer.rendering.*;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class RareCandyCanvas {
    private static CycleVariants runnable;
    public static Matrix4f projectionMatrix;
    public static float radius = 2.0f;

    private final ModelLoader loader = new ModelLoader();

    public static float lightLevel = 1;
    private static double time;
    public static FrameBuffer framebuffer;

    public static final Matrix4f viewMatrix = new Matrix4f();
    public final List<AnimatedObjectInstance> instances = new ArrayList<>();
    public float scaleModifier = 0;
    private final PokeUtilsGui handler;
    public double startTime = System.currentTimeMillis();
    public String currentAnimation = null;
    public double originalScaleModifer;
    private RareCandy renderer;
//    private MultiRenderObject<MeshObject> plane;
//    private ObjectInstance planeInstance;

    public ToggleableMultiRenderObject loadedModel;
    public AnimatedObjectInstance loadedModelInstance;
    private static float previousLightLevel;
    private String fileName;
    public static boolean cycling;
    private ScreenRenderer screenRenderer;
    public static boolean animate = true;
    public static boolean renderingFrame;
//    private MultiRenderObject<MeshObject> cube;
//    private ObjectInstance[] cubeInstances;
    private FogUploader fogUploader;
    private boolean initCalled;

    public static void setLightLevel(float lightLevel) {
        previousLightLevel = RareCandyCanvas.lightLevel;
        RareCandyCanvas.lightLevel = lightLevel;
    }

     static float getLightLevel() {
        return lightLevel;
    }

    public RareCandyCanvas(PokeUtilsGui handler) {
        this.handler = handler;
        resize(handler.getWidth(), handler.getHeight());

    }

    public void resize(int width, int height) {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) width / height, 0.1f, 1000.0f);
    }


    public static double getTime() {
        return time;
    }

    public void openFile(PixelAsset pkFile, String name) throws IOException {
        openFile(pkFile, name, () -> {}, true);
    }

    public void openFile(PixelAsset pkFile, String name, Runnable runnable, boolean resetAnimation) throws IOException {
        currentAnimation = null;
//        renderer.objectManager.clearObjects();
//        renderer.objectManager.add(plane, planeInstance);

//        for (ObjectInstance instance : cubeInstances) {
//            renderer.objectManager.add(cube, instance);
//        }

        if(loadedModel != null) loadedModel.close();

        this.fileName = name;

        if(pkFile == null) return;

        loadPokemonModel(pkFile, model -> {
            var i = 0;

            loadedModel = (ToggleableMultiRenderObject) model;

            scaleModifier = loadedModel.scale;
            originalScaleModifer = loadedModel.scale;

            handler.fileViewer.scale.reset();

            var variants = model.availableVariants();

            var variant = !variants.isEmpty() ? variants.iterator().next() : null;
            var instance = new AnimatedObjectInstance(new Matrix4f(), variant);

            loadedModelInstance = renderer.objectManager.add(model, instance);
            model.updateDimensions();
            runnable.run();

            if(resetAnimation) setAnimation("idle");

        });
    }

    public void initGL() {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) handler.getWidth() / handler.getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize(this, handler.settings);
        this.renderer = new RareCandy();

        fogUploader = new FogUploader(handler.settings.fog);

        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        framebuffer = new FrameBuffer(1024, 1024);

//        screenRenderer = new ScreenRenderer(framebuffer);


//        loadPlane(100, 100, model -> {
//            plane = model;
//            planeInstance = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0f, -0.001f, 0f), "plane"));
//        });

//        loadCube(1, 1, 1, model -> {
//            cube = model;
//            cubeInstances = new ObjectInstance[4];
//            cubeInstances[0] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, -0.5f, 0), viewMatrix, null));
//            cubeInstances[1] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, 0.5f, -1), viewMatrix, null));
//            cubeInstances[2] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, 1.5f, -1), viewMatrix, null));
//            cubeInstances[3] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, 02.5f, -1), viewMatrix, null));
//        });
    }

    private MultiRenderObject<MeshObject> loadPlane(int width, int length, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        return loader.generatePlane(width, length, onFinish);
    }

    private MultiRenderObject<MeshObject> loadCube(int width, int length, int height, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        return loader.generateCube(width, length, height, "smooth_stone", onFinish);
    }


    private final Vector3f size = new Vector3f();

    private final double fraciton = 1/16f;
//    @Override
    public void render() {
        if (loadedModelInstance != null) {
            loadedModelInstance.transformationMatrix().identity().scale(scaleModifier);

            size.set(loadedModel.dimensions).mul(scaleModifier);
        }

        if(animate) time = (System.currentTimeMillis() - startTime) / 1000f;

        if (runnable != null) runnable.pre();

        renderer.update(time);

        var pipeline = PipelineRegistry.get("animated");

        pipeline.useProgram();
//        renderToFramebuffer();
        pipeline.bindGlobal(null, null);

        renderToScreen(pipeline);

        if (runnable != null) runnable.post();

        if (instances.size() > 1) {
            ((MultiRenderObject<AnimatedMeshObject>) instances.get(0).object()).onUpdate(a -> {
                for (var instance : instances) {
                    if(a.animations != null) {
                        var newAnimation = a.animations.get(currentAnimation);

                        if(newAnimation != null) {
                            instance.changeAnimation(createInstance(newAnimation));
                        }
                    }
                }
            });
        }
    }

    private final int[] originalViewport = new int[4]; // Array to store x, y, width, height

    private void renderToFramebuffer() {
//        renderingFrame = true;
//        framebuffer.bindFramebuffer();
//
//        glGetIntegerv(GL_VIEWPORT, originalViewport);
//
//        GL11C.glViewport(0, 0, 1024, 1024);
//
//        renderer.render(RenderStage.SOLID, false, time);
//        renderer.render(RenderStage.TRANSPARENT, false, time);
//
//        framebuffer.unbindFramebuffer();
//
//        glViewport(originalViewport[0], originalViewport[1], originalViewport[2], originalViewport[3]);
//        renderingFrame = false;
    }

    private void renderToScreen(Pipeline pipeline) {
        renderer.render(pipeline, RenderStage.SOLID, false);
        renderer.render(pipeline, RenderStage.TRANSPARENT, false);
    }

    public AnimationInstance createInstance(Animation animation) {
        return new AnimationInstance(animation);
    }

    protected void loadPokemonModel(PixelAsset is, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        loader.createObject(
                ToggleableMultiRenderObject::new,
                () -> is,
                (gltfModel, animResources, images, config, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    ModelLoader.processModel(object, gltfModel, animResources, images, config, glCalls, AnimatedMeshObject::new, GLModel::new);
                    return glCalls;
                }, onFinish);
    }

    public void setAnimation(@NotNull String animation) {
        AnimatedMeshObject object = loadedModel.objects.get(0);

        if (Objects.requireNonNull(object.animations).containsKey(animation)) {
            loadedModelInstance.changeAnimation(createInstance(object.animations.get(animation)));
        }
    }

    public void updateLoadedModel(Consumer<AnimatedMeshObject> consumer) {
        loadedModel.onUpdate(consumer);
    }

    public void setVariant(String variant) {
        loadedModelInstance.setVariant(variant);
    }

    public void toggleObject(boolean add, String object) {
        if(loadedModel.overrides.contains(object)) {
            if(add) {
                loadedModel.overrides.remove(object);
            }
        } else if(!add) {
            loadedModel.overrides.add(object);
        }
    }

    public static final Path images = Path.of("assets", "generations_core", "textures", "pokemon");

    private Path root = Path.of("images");

    public void takeScreenshot(boolean isPortrait) throws IOException {
        var path = root.resolve(fileName);
        if(Files.notExists(path)) Files.createDirectories(path);

        var temp = Path.of((path + "\\" + (isPortrait ? "portrait" : "profile") + "-" + (loadedModelInstance.variant() != null ? loadedModelInstance.variant() : "default") + ".png").replace("\\", "/"));
        if(framebuffer.captureScreenshot(temp, isPortrait)) {
            LoggerUtil.print("Screenshot saved to " + temp);
        } else {
            LoggerUtil.print("Failed to save screenshot to " + temp);
        }
    }

    public boolean isInitCalled() {
        return initCalled;
    }

    public FogUploader getFogUploader() {
        return fogUploader;
    }

    public static class CycleVariants  {
        private final RareCandyCanvas canvas;
        private final boolean isPortrait;
        private List<String> list;
        private int index;

        public CycleVariants(RareCandyCanvas canvas, boolean isPortrait) {
            this.canvas = canvas;
            this.isPortrait = isPortrait;
            if(canvas.loadedModel == null) return;
            RareCandyCanvas.cycling = true;
            list = List.copyOf(canvas.loadedModel.availableVariants());

            if(list.size() == 0) return;

            index = 0;
            RareCandyCanvas.runnable = this;
        }
        public void pre() {
            if(index >= list.size()) {
                RareCandyCanvas.runnable = null;
                cycling = false;
            } else {
                canvas.loadedModelInstance.setVariant(list.get(index));
            }
        }

        public void post() {
            try {
                canvas.takeScreenshot(isPortrait);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            index += 1;
        }
    }

    public static class ToggleableMultiRenderObject extends MultiRenderObject<AnimatedMeshObject> {
        public List<String> overrides = new ArrayList<>();

        @Override
        public <V extends RenderObject> void render(Pipeline pipeline, RenderStage stage, List<ObjectInstance> instances) {
            for (var object : this.objects) {
                if (object != null && !overrides.contains(object.name) && object.isReady()) {
                    object.render(pipeline, stage, instances);
                }
            }
        }

    }
}

class FogUploader extends UniformBlockUploader {
    private final long pointer;

    public FogUploader(PokeUtilsGui.Settings.Fog fog) {
        super(VEC4_SIZE + 2 * Float.BYTES + Integer.BYTES + 4);
        this.pointer = MemoryUtil.nmemAlloc(VEC4_SIZE + 2 * Float.BYTES + Integer.BYTES);
        update(fog);
        fog.setListener(this::update);
    }

    private void update(PokeUtilsGui.Settings.Fog fog) {
        var clear = fog.color.getValue();

        fog.color.getValue().getToAddress(pointer);
        MemoryUtil.memPutFloat(pointer + 16, fog.start.floatValue());
        MemoryUtil.memPutFloat(pointer + 20, fog.end.floatValue());
        MemoryUtil.memPutInt(pointer + 24, 0);

        upload(0, 28, pointer);

    }
}

