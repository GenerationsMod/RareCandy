package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.renderer.textures.FrameBuffer;
import gg.generations.rarecandy.renderer.textures.ITexture;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PassChain implements AutoCloseable {
    private final List<FullscreenPass> passes = new ArrayList<>();

    private final FrameBuffer a;
    private final FrameBuffer b;

    private int width;
    private int height;

    private final Map<String, FrameBuffer> named = new HashMap<>();
    private final Map<String, Float> namedScales = new HashMap<>();

    public PassChain(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);

        a = FrameBuffer.soloAlbedo(this.width, this.height);
        b = FrameBuffer.soloAlbedo(this.width, this.height);
    }

    private static int scaled(int value, float scale) {
        return Math.max(1, Math.round(value * scale));
    }

    public PassChain add(FullscreenPass pass) {
        String key = pass.getOutput();

        if (named.containsKey(key)) {
            throw new IllegalArgumentException("A pass with the key '" + key + "' already exists; each one must be unique.");
        }

        namedScales.put(key, pass.getScale());
        named.put(key, FrameBuffer.soloAlbedo(scaled(width, pass.getScale()), scaled(height, pass.getScale())));

        passes.add(pass);
        return this;
    }


    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        a.resize(Math.max(1, width), Math.max(1, height));
        b.resize(Math.max(1, width), Math.max(1, height));

        named.forEach((key, fb) -> {
            float scale = namedScales.get(key);
            fb.resize(scaled(width, scale), scaled(height, scale));
        });
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

        FrameBuffer previous = gbuffer;

        for (var pass : active) {
            FrameBuffer target = pass.getOutput() != null ? named.get(pass.getOutput()) : previous == a ? b : a;

            pass.render(gbuffer, previous, named, target);
            previous = target;
        }

        active.getLast().renderToScreen(gbuffer, previous, named, width, height);
    }

    @Override
    public void close() {
        passes.forEach(FullscreenPass::destroy);
        a.close();
        b.close();
        named.values().forEach(FrameBuffer::close);
        named.clear();
    }
}