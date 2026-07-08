package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingRunnable;
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingTriConsumer;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.components.DummyVAO;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.loading.AnimResource;
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler;
import gg.generations.rarecandy.renderer.loading.Names;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.rendering.*;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class RareCandyCanvas {
    private static final int DEFERRED_COLOR = 0;
    private static final int DEFERRED_NORMAL = 1;
    private static final int DEFERRED_OBJECT = 2;

    private static CycleVariants runnable;
    public static Matrix4f projectionMatrix;
    public static float radius = 2.0f;

    public static float lightLevel = 1;
    private static double time;
    public static FrameBuffer framebuffer;

    public static final Matrix4f viewMatrix = new Matrix4f();
    public final List<AnimatedObjectInstance> instances = new ArrayList<>();
    public float scaleModifier = 0;
    public final Vector3f modelTranslation = new Vector3f();
    public float modelYaw = 0.0f;
    public final Vector4f outlineColor = new Vector4f(1, 1, 1, 1);
    public float outlineThickness = 3.25f;
    public float outlineDepthThreshold = 0.0025f;
    public float outlineNormalThreshold = 0.35f;
    private final PokeUtilsGui handler;
    public double startTime = System.currentTimeMillis();
    public String currentAnimation = null;
    public double originalScaleModifer;
    public IModelConfig config;
    private RareCandy renderer;

    private DummyVAO vao;

    public BaseMultiRenderObject loadedModel;
    public AnimatedObjectInstance loadedModelInstance;
    private static float previousLightLevel;
    private String fileName;
    public static boolean cycling;
    private ScreenRenderer screenRenderer;
    public static boolean animate = true;
    public static boolean renderingFrame;
    private FogUploader fogUploader;
    private ScreenSpaceGridRenderer gridRenderer;
    private boolean initCalled;
    private boolean rendering = false;

    private final StateManager manager = new StateManager(
            BlendType.Regular::enable, BlendType.Regular::disable,
            CullType.Forward::enable, CullType.Forward::disable,
            () -> GL11.glEnable(GL11.GL_DEPTH_TEST), () -> GL11.glDisable(GL11.GL_DEPTH_TEST)
    );
    public Selected selected;
    private Skeleton loadedSkeleton;
    private Map<String, AnimResource> loadedAnimationSources;

    public static void setLightLevel(float lightLevel) {
        previousLightLevel = RareCandyCanvas.lightLevel;
        RareCandyCanvas.lightLevel = lightLevel;
    }

    public RareCandyCanvas(PokeUtilsGui handler) {
        this.handler = handler;
        this.selected = new Selected(this);
        resize(handler.getWidth(), handler.getHeight());
    }

    public void resize(int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);

        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) safeWidth / safeHeight, 0.1f, 1000.0f);

        if (framebuffer != null) {
            framebuffer.resize(safeWidth, safeHeight);
        }
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
        final BaseMultiRenderObject oldModel = loadedModel;
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

            config = IModelConfig.from(pkFile);

            loadPokemonModel(pkFile, (model, skeleton, names) -> {
                loadedModel = (BaseMultiRenderObject) model;
                loadedSkeleton = skeleton;

                resetModelTransform();

                scaleModifier = loadedModel.scale;
                originalScaleModifer = loadedModel.scale;

                handler.fileViewer.scale.reset();

                var variants = model.availableVariants();
                var variant = !variants.isEmpty() ? variants.iterator().next() : null;

                var instance = new AnimatedObjectInstance(new Matrix4f(), new Matrix3f(), loadedModel.variantNameToId.get(variant));
                loadedModelInstance = renderer.add(model, instance);
                loadedModelInstance.use();

                runnable.run();

                if (resetAnimation) setAnimation("idle");

                rendering = true;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initGL() {

        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) handler.getWidth() / handler.getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize(this, handler.settings);
        this.renderer = new RareCandy(

        );

        fogUploader = new FogUploader(handler.settings.fog);
        gridRenderer = new ScreenSpaceGridRenderer();

        framebuffer = createDeferredFramebuffer(handler.getWidth(), handler.getHeight());

        vao = new DummyVAO();
    }


    private final Vector3f size = new Vector3f();

    private final double fraciton = 1/16f;
//    @Override
    public void render() {
        if(!rendering) return;

        if (loadedModelInstance != null) {
            loadedModelInstance.modelMatrix().identity()
                    .translate(modelTranslation)
                    .rotateY(modelYaw)
                    .scale(scaleModifier);
            loadedModelInstance.normalMatrix().identity().rotateY(modelYaw);
            loadedModelInstance.use();
            size.set(loadedModel.dimensions).mul(scaleModifier);

        }

        if(animate) time = (System.currentTimeMillis() - startTime) / 1000f;
//        renderer.update(time);

        if (runnable != null) runnable.pre();

        renderer.update(time);

        vao.bind();

        renderDeferred();
        renderGrid();

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

    private static FrameBuffer createDeferredFramebuffer(int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);

        return FrameBuffer.builder(safeWidth, safeHeight)
                .color(FrameBuffer.TextureSpec.texture2D(ITexture.Type.RGBA8).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
                .color(FrameBuffer.TextureSpec.texture2D(ITexture.Type.RGBA16F).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
                .color(FrameBuffer.TextureSpec.texture2D(ITexture.Type.R32F).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
                .depthTexture(FrameBuffer.TextureSpec.depth2D(ITexture.Type.DEPTH24, false))
                .build();
    }

    private void renderDeferred() {
        renderingFrame = true;
        GL11C.glGetIntegerv(GL11C.GL_VIEWPORT, originalViewport);

        try {
            framebuffer.bindAndSetViewport();
            framebuffer.setDrawAll();
            clearDeferredFramebuffer();

            GuiPipelines.DEFERRED.useProgram();
            GuiPipelines.DEFERRED.bindGlobal();
            renderer.render(GuiPipelines.DEFERRED, manager);
        } finally {
            FrameBuffer.unbindFramebuffer();
            GL11C.glViewport(originalViewport[0], originalViewport[1], originalViewport[2], originalViewport[3]);
            renderingFrame = false;
        }

        renderDeferredToScreen();
        copyDeferredDepthToScreen();
    }

    private void clearDeferredFramebuffer() {
        var clear = handler.settings.fog.color.getValue();
        framebuffer.clearColor(DEFERRED_COLOR, clear.x, clear.y, clear.z, clear.w);
        framebuffer.clearColor(DEFERRED_NORMAL, 0.5f, 0.5f, 0.5f, 0.0f);
        framebuffer.clearColor(DEFERRED_OBJECT, 0.0f, 0.0f, 0.0f, 0.0f);
        framebuffer.clearDepth(1.0f);
    }

    private void renderDeferredToScreen() {
        manager.reset();
        GuiPipelines.DEFERRED_COMPOSITE.useProgram();
        GuiPipelines.DEFERRED_COMPOSITE.bindGlobal();

        vao.bind();
        GL11C.glDrawArrays(GL11C.GL_TRIANGLES, 0, 3);
        vao.unbind();
    }

    private void copyDeferredDepthToScreen() {
        GL45C.glBlitNamedFramebuffer(
                framebuffer.getFramebufferId(),
                0,
                0, 0, framebuffer.width(), framebuffer.height(),
                0, 0, handler.getWidth(), handler.getHeight(),
                GL11C.GL_DEPTH_BUFFER_BIT,
                GL11C.GL_NEAREST
        );
    }

    private void renderGrid() {
        if (gridRenderer != null) {
            gridRenderer.render(handler.getWidth(), handler.getHeight(), handler.settings, manager);
        }
    }

    public void resetModelTransform() {
        modelTranslation.zero();
        modelYaw = 0.0f;
    }

    public void stopRenderingAfterFailure() {
        rendering = false;
    }

    public AnimationInstance createInstance(Animation animation) {
        return new AnimationInstance(animation);
    }

    protected void loadPokemonModel(ResourceReader is, ExceptionThrowingTriConsumer<MultiRenderObject, Skeleton, Map<String, AnimResource>> onFinish) throws Exception {
        ModelObjectCompiler.buildObject(
                BaseMultiRenderObject::new,
                () -> config,
                () -> is,
                (reader, images, layer) -> ModelObjectCompiler.readImages(reader, images, layer, true),
                IMaterialReference::process,
                onFinish
        );
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

        public BaseMultiRenderObject(Names names) {
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


