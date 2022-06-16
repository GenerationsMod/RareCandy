package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.rarecandy.components.RenderObject;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

public class RenderScene {

    private final List<RenderObject> objects = new ArrayList<>(); // TODO: instancing
    public final CompatibilityProvider provider;

    public RenderScene(CompatibilityProvider provider) {
        this.provider = provider;
    }

    public void add(RenderObject object) {
        this.objects.add(object);
    }

    public List<RenderObject> getObjects() {
        return this.objects;
    }

    public void render(boolean updateState) {
        for (RenderObject object : this.objects) {
            object.render(this.provider.getProjectionMatrix(), this.provider.getViewMatrix());
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
    }

    public void preRender() {
        for (RenderObject object : this.objects) {
            object.update();
        }
    }
}
