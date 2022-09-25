package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.settings.Settings;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RareCandy {

    private final ExecutorService modelLoadingPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 4);
    private final Settings settings;
    private final Map<RenderObject, List<InstanceState>> objectMap = new HashMap<>();

    public RareCandy(Settings settings) {
        this.settings = settings;
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

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }
}
