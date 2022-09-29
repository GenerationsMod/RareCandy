package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.loading.PKLoader;
import com.pixelmongenerations.rarecandy.settings.Settings;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RareCandy {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
    private final PKLoader loader;
    private final Map<RenderObject, List<InstanceState>> objectMap = new HashMap<>();

    public RareCandy(Settings settings) {
        var startLoad = System.currentTimeMillis();
        this.loader = new PKLoader(settings);
        LOGGER.info("RareCandy Startup took " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public void addObject(RenderObject object, InstanceState state) {
        this.objectMap.putIfAbsent(object, new ArrayList<>());
        List<InstanceState> instances = this.objectMap.get(object);
        instances.add(state);
    }

    public void render(boolean updateState, boolean clearInstances) {
        for (RenderObject object : this.objectMap.keySet()) {
            if (object.isReady()) {
                object.update();
                object.render(this.objectMap.get(object));
            }
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

    public void close() {
        this.loader.close();
    }

    public List<InstanceState> getObjects() {
        List<InstanceState> instances = new ArrayList<>();
        for (RenderObject object : this.objectMap.keySet()) instances.addAll(this.objectMap.get(object));
        return instances;
    }

    public PKLoader getLoader() {
        return loader;
    }

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }

}
