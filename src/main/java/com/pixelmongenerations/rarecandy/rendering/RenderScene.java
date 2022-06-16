package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.rarecandy.components.RenderObject;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some nice scene code in the mess which is Minecraft's rendering system. It's the best way I can see to implement batch rendering without messing with Minecraft too much :p
 * We need to be careful with this because we may leave old objects in here which slows this down over time. Not sure how to handle that, honestly.
 */
public class RenderScene {

    private final Map<RenderObject, List<InstanceState>> objectMap = new HashMap<>();
    public final CompatibilityProvider provider;

    public RenderScene(CompatibilityProvider provider) {
        this.provider = provider;
    }

    public void add(RenderObject object, InstanceState state) {
        this.objectMap.putIfAbsent(object, new ArrayList<>());
        List<InstanceState> instances = this.objectMap.get(object);
        instances.add(state);
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

    public void preRender() {
        for (RenderObject object : this.objectMap.keySet()) {
            object.update();
        }
    }

    public List<InstanceState> getAllInstances() {
        List<InstanceState> instances = new ArrayList<>();
        for (RenderObject object : this.objectMap.keySet()) {
            instances.addAll(this.objectMap.get(object));
        }

        return instances;
    }
}
