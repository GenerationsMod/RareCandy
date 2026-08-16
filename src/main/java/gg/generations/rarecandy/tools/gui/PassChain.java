package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import gg.generations.rarecandy.renderer.textures.ITexture;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

public final class PassChain implements AutoCloseable {
    private final List<FullscreenPass> passes = new ArrayList<>();

    private final FrameBuffer a;
    private final FrameBuffer b;

    public PassChain(int width, int height) {
        a = createTarget(width, height);
        b = createTarget(width, height);
    }

    private static FrameBuffer createTarget(int width, int height) {
        return FrameBuffer.builder(width, height)
                .color(FrameBuffer.TextureSpec.texture2D(ITexture.Type.RGBA16F))
                .build();
    }

    public PassChain add(FullscreenPass pass) {
        passes.add(pass);
        return this;
    }

    public void resize(int width, int height) {
        a.resize(width, height);
        b.resize(width, height);
    }

    public void reload() {
        passes.forEach(FullscreenPass::reload);
    }

    public void render(FrameBuffer gbuffer, int width, int height) {
        List<FullscreenPass> active = passes.stream().filter(FullscreenPass::isEnabled).toList();

        if (active.isEmpty()) {
            gbuffer.blitToScreen(0, width, height, GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_NEAREST);
            return;
        }

        // previous starts as the gbuffer, so Source.Previous on the first pass means albedo.
        FrameBuffer previous = gbuffer;

        for (int i = 0; i < active.size() - 1; i++) {
            FrameBuffer target = (previous == a) ? b : a;
            active.get(i).render(gbuffer, previous, target);
            previous = target;
        }

        active.get(active.size() - 1).renderToScreen(gbuffer, previous, width, height);
    }

    @Override
    public void close() {
        a.close();
        b.close();
    }
}