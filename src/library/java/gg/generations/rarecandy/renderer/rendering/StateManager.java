package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import org.lwjgl.opengl.GL11;

public class StateManager {
    private final StateToggle blend;
    private final StateToggle cull;
    private final StateToggle depth;

    public StateManager(Runnable blendEnable, Runnable blendDisable, Runnable cullEnable, Runnable cullDisable, Runnable depthEnable, Runnable depthDisable) {
        blend = new StateToggle(blendEnable, blendDisable);
        cull = new StateToggle(cullEnable, cullDisable);
        depth = new StateToggle(depthEnable, depthDisable);
    }

    public void toggle(boolean isBlend, boolean isCull, boolean isDepth) {
        blend.toggle(isBlend);
        cull.toggle(isCull);
        depth.toggle(isDepth);
    }

    public void toggle(RenderStage stage) {
        toggle(stage.isBlend(), stage.isCull(), stage.isDepthTest());
    }

    public void reset() {
        toggle(false, false, false);
    }
}
