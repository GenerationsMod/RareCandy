package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class RareCandy {

    private final ExecutorService modelLoadingPool;
    private final Settings settings;
    private final Map<RenderObject, List<InstanceState>> objectMap = new HashMap<>();
    private final RenderObject fallbackModel;

    public RareCandy(Settings settings, Supplier<Matrix4f> projectionMatrix) {
        this.settings = settings;
        this.modelLoadingPool = Executors.newFixedThreadPool(settings.modelLoadingThreads());
        var model = new PixelAsset(Objects.requireNonNull(RareCandy.class.getResourceAsStream("/fallback/loading_text.glb"), "Fallback Model Missing!"), PixelAsset.Type.GLB);
        this.fallbackModel = model.createStaticObject(Pipeline.fallback(projectionMatrix));
    }

    public void addObject(RenderObject object, InstanceState state) {
        this.objectMap.putIfAbsent(object, new ArrayList<>());
        List<InstanceState> instances = this.objectMap.get(object);
        instances.add(state);
    }

    public void preRender() {
        for (RenderObject object : this.objectMap.keySet()) {
            object.update();
        }
    }

    public void render(boolean updateState, boolean clearInstances) {
        for (RenderObject object : this.objectMap.keySet()) {
            object.render(this.objectMap.get(object));
        }

        if (updateState) {
            GL11C.glEnable(GL11C.GL_BLEND);
            GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
            GL11C.glDepthMask(false);
            GL11C.glDepthFunc(GL11C.GL_EQUAL);
            GL11C.glDepthFunc(GL11C.GL_LESS);
            GL11C.glDepthMask(true);
            GL11C.glDisable(GL11C.GL_BLEND);
        }

        if (clearInstances) {
            this.objectMap.clear();
        }
    }

    public List<InstanceState> getObjects() {
        List<InstanceState> instances = new ArrayList<>();
        for (RenderObject object : this.objectMap.keySet()) instances.addAll(this.objectMap.get(object));
        return instances;
    }

    public RenderObject createModel(@NotNull InputStream is, PixelAsset.Type type, Pipeline pipeline) {
        PixelAsset model = new PixelAsset(is, PixelAsset.Type.PK);
        return model.createAnimatedObject(pipeline);
    }

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }

}
