package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.pkl.reader.GlbReader;
import com.pixelmongenerations.rarecandy.OldModelLoader;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import com.pixelmongenerations.rarecandy.settings.Settings;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class RareCandy {

    private final ExecutorService modelLoadingPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 4);
    private final RenderObject placeholderObject;
    private final Settings settings;
    private final Map<RenderObject, List<InstanceState>> objectMap = new HashMap<>();
    public final GameInterface provider;

    public RareCandy(Settings settings, GameInterface provider) {
        this.settings = settings;
        this.provider = provider;
        this.placeholderObject = loadPlaceholderModel();
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
            object.render(this.provider.getProjectionMatrix(), this.objectMap.get(object));
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

    public RenderObject loadPlaceholderModel() {
        try {
            var scene = new GlbReader().read(Objects.requireNonNull(RareCandy.class.getResourceAsStream("/fallback/loading_text.glb"), "Placeholder Model is Missing. Cannot Continue!"));
            return OldModelLoader.loadStaticFile(scene, ShaderProgram.FALLBACK);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Placeholder model. Cannot Continue!", e);
        }
    }

    public List<InstanceState> getObjects() {
        List<InstanceState> instances = new ArrayList<>();
        for (RenderObject object : this.objectMap.keySet()) instances.addAll(this.objectMap.get(object));
        return instances;
    }

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! \"" + message + "\"");
    }
}
