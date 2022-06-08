package com.pixelmongenerations.rarecandy.rendering;

import com.pixelmongenerations.rarecandy.core.RenderObject;
import org.lwjgl.opengl.GL11C;

public class RenderingEngine {

    public final GameProvider provider;

    public RenderingEngine(GameProvider provider) {
        this.provider = provider;
    }

    public void render(RenderObject object) {
        object.render(this);
        GL11C.glEnable(GL11C.GL_BLEND);
        GL11C.glBlendFunc(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA);
        GL11C.glDepthMask(false);
        GL11C.glDepthFunc(GL11C.GL_EQUAL);
        GL11C.glDepthFunc(GL11C.GL_LESS);
        GL11C.glDepthMask(true);
        GL11C.glDisable(GL11C.GL_BLEND);
    }
}
