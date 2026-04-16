package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.MaterialReference;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingBiFunction;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingConsumer;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingRunnable;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.components.DummyVAO;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.rendering.*;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import gg.generations.rarecandy.renderer.textures.TextureArray;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static gg.generations.rarecandy.renderer.loading.ModelLoader.createObject;
import static org.lwjgl.opengl.GL42C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;


public class RareCandyCanvas {
    private static CycleVariants runnable;
    public static Matrix4f projectionMatrix;
    public static float radius = 2.0f;

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

    private DummyVAO vao;

    public ToggleableMultiRenderObject loadedModel;
    public AnimatedObjectInstance loadedModelInstance;
    private static float previousLightLevel;
    private String fileName;
    public static boolean cycling;
    private ScreenRenderer screenRenderer;
    public static boolean animate = true;
    public static boolean renderingFrame;
    private FogUploader fogUploader;
    private boolean initCalled;
    private boolean rendering = false;

    private StateManager manager = new StateManager(
            BlendType.Regular::enable, BlendType.Regular::disable,
            CullType.Forward::enable, CullType.Forward::disable,
            () -> GL11.glEnable(GL11.GL_DEPTH_TEST), () -> GL11.glDisable(GL11.GL_DEPTH_TEST)
    );

    public static void setLightLevel(float lightLevel) {
        previousLightLevel = RareCandyCanvas.lightLevel;
        RareCandyCanvas.lightLevel = lightLevel;
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

    public void openFile(ResourceReader pkFile, String name) throws Exception {
        openFile(pkFile, name, () -> {}, true);
    }

    public void openFile(ResourceReader pkFile, String name, ExceptionThrowingRunnable runnable, boolean resetAnimation) throws Exception {
        currentAnimation = null;
        rendering = false;

        // Detach references immediately so render() will not touch freed objects
        final ToggleableMultiRenderObject oldModel = loadedModel;
        final AnimatedObjectInstance oldInstance = loadedModelInstance;
        final List<AnimatedObjectInstance> oldInstances = new ArrayList<>(instances);

        loadedModel = null;
        loadedModelInstance = null;
        instances.clear();

        this.fileName = name;

        try {
            if (oldModel != null) {
                // Ensure renderer no longer references the object BEFORE deleting its GL resources
                renderer.remove(oldModel); // must only detach, not close

                // Close instances first (if they own per-instance GL objects), then the model
                for (var inst : oldInstances) {
                    inst.close();
                }

                oldModel.close();
            }

            if (pkFile == null) return;

            loadPokemonModel(pkFile, model -> {
                loadedModel = (ToggleableMultiRenderObject) model;

                scaleModifier = loadedModel.scale;
                originalScaleModifer = loadedModel.scale;

                handler.fileViewer.scale.reset();

                var variants = model.availableVariants();
                var variant = !variants.isEmpty() ? variants.iterator().next() : null;

                var instance = new AnimatedObjectInstance(new Matrix4f(), new Matrix3f(), loadedModel.variantNameToId.get(variant));
                loadedModelInstance = renderer.add(model, instance);
                loadedModelInstance.use();

                model.updateDimensions();
                runnable.run();

                if (resetAnimation) setAnimation("idle");

                rendering = true;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initGL() {
//        if (!GL.getCapabilities().GL_ARB_bindless_texture) {
//            throw new RuntimeException("Bindless textures not supported!");
//        }

        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) handler.getWidth() / handler.getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize(this, handler.settings);
        this.renderer = new RareCandy(

        );

        fogUploader = new FogUploader(handler.settings.fog);

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

        vao = new DummyVAO();
    }

//    private MultiRenderObject loadPlane(int width, int length, Consumer<MultiRenderObject> onFinish) {
//        return loader.generatePlane(width, length, onFinish);
//    }

//    private MultiRenderObject loadCube(int width, int length, int height, Consumer<MultiRenderObject> onFinish) {
//        return loader.generateCube(width, length, height, "smooth_stone", onFinish);
//    }


    private final Vector3f size = new Vector3f();

    private final double fraciton = 1/16f;
//    @Override
    public void render() {
        if(!rendering) return;

//        if(planeInstance != null) planeInstance.use();

        if (loadedModelInstance != null) {
            loadedModelInstance.modelMatrix().identity().scale(scaleModifier);
            loadedModelInstance.use();
            size.set(loadedModel.dimensions).mul(scaleModifier);

        }

        if(animate) time = (System.currentTimeMillis() - startTime) / 1000f;
//        renderer.update(time);

        if (runnable != null) runnable.pre();

        renderer.update(time);

        vao.bind();

//        renderToFramebuffer();

        renderToScreen();

        renderer.end();

        if (runnable != null) runnable.post();

        if (instances.size() > 1) {

            (instances.get(0).object()).onUpdate(a -> {
                for (var instance : instances) {
                    if(a.animations != null) {
                        var newAnimation = a.animationNameToId.getOrDefault(currentAnimation, -1);

                        if(newAnimation > -1) {
                            instance.changeAnimation(createInstance(a.animations[newAnimation]));
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

    private void renderToScreen() {
        GuiPipelines.ANIMATED.useProgram();
        GuiPipelines.ANIMATED.bindGlobal();

        renderer.render(GuiPipelines.ANIMATED, manager);
    }

    public AnimationInstance createInstance(Animation animation) {
        return new AnimationInstance(animation);
    }

    protected void loadPokemonModel(ResourceReader is, ExceptionThrowingConsumer<MultiRenderObject> onFinish) throws Exception {
        createObject(
                ToggleableMultiRenderObject::new,
                () -> is, ModelLoader::readImages, MaterialReference::process, onFinish);
    }

    public void setAnimation(@NotNull String animation) {
        var animId = loadedModel.animationNameToId.getOrDefault(animation, -1);

        if (animId > -1) {
            loadedModelInstance.changeAnimation(createInstance(loadedModel.animations[animId]));
        }
    }

    public void updateLoadedModel(Consumer<MultiRenderObject> consumer) {
        loadedModel.onUpdate(consumer);
    }

    public void setVariant(String variant) {
        loadedModelInstance.setVariant(loadedModel.variantNameToId.get(variant));
    }

    public void toggleObject(boolean add, String object) {
        var mesh = loadedModel.meshNameToId.get(object);

        if(loadedModel.overrides[mesh]) {
            if(add) {
                loadedModel.overrides[mesh] = false;
            }
        } else if(!add) {
            loadedModel.overrides[mesh] = true;
        }
    }

    public static final Path images = Path.of("assets", "generations_core", "textures", "pokemon");

    private Path root = Path.of("images");

    public void takeScreenshot(boolean isPortrait) throws IOException {
//        var path = root.resolve(fileName);
//        if(Files.notExists(path)) Files.createDirectories(path);
//
//        var temp = Path.of((path + "\\" + (isPortrait ? "portrait" : "profile") + "-" + (loadedModel.variantNameToId.get(loadedModelInstance.variant()) != null ? loadedModelInstance.variant() : "default") + ".png").replace("\\", "/"));
//        if(framebuffer.captureScreenshot(temp, isPortrait)) {
//            LoggerUtil.print("Screenshot saved to " + temp);
//        } else {
//            LoggerUtil.print("Failed to save screenshot to " + temp);
//        }
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
                canvas.loadedModelInstance.setVariant(index);
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

    public static class BaseMultiRenderObject extends MultiRenderObject {

        public BaseMultiRenderObject(ModelLoader.Names names) {
            super(names);
        }

        @Override
        public void render(TraditionalPipeline pipeline, RenderStage stage, List<ObjectInstance> instances) {
            var buffer = drawBuffer.get(stage);
            if(buffer == null) return;
            buffer.render();
        }

        @Override
        public int targetVertexStride() {
            return 32;
        }
    }

    public class ToggleableMultiRenderObject extends BaseMultiRenderObject {
        public boolean[] overrides;
        public String[] meshNames;

        public ToggleableMultiRenderObject(ModelLoader.Names names) {
            super(names);
            overrides = new boolean[names.meshes().size()];
            meshNames = names.meshes().toArray(String[]::new);
        }

        @Override
        public boolean shouldRender(int mesh, ObjectInstance instance) {
            return super.shouldRender(mesh, instance) || overrides[mesh];
        }
    }
}

class FogUploader extends UniformBlockUploader {
    private final long pointer;

    public FogUploader(PokeUtilsGui.Settings.Fog fog) {
        super(VEC4_SIZE + 2 * Float.BYTES + Integer.BYTES + 4, 0);
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


