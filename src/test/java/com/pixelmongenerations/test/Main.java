package com.pixelmongenerations.test;

import com.pixelmongenerations.pixelmonassetutils.PixelAsset;
import com.pixelmongenerations.pixelmonassetutils.reader.GlbReader;
import com.pixelmongenerations.rarecandy.AnimationUtil;
import com.pixelmongenerations.rarecandy.core.RenderObject;
import com.pixelmongenerations.rarecandy.rendering.GameProvider;
import com.pixelmongenerations.rarecandy.rendering.RenderingEngine;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main implements GameProvider {

    public final Window window;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    private final RenderingEngine renderer;
    public final RenderObject root;
    private final List<RenderObject> displayPokemons = new ArrayList<>();

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        if (Files.exists(Paths.get("C:/Program Files/RenderDoc/renderdoc.dll"))) {
            System.load("C:/Program Files/RenderDoc/renderdoc.dll");
        }
        this.window = new Window("Pixelmon Renderer Test", 960, 540);
        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);
        this.viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        this.renderer = new RenderingEngine(this);
        this.root = new RenderObject();
        setupRenderer();

        for (int x = -5; x < 10; x++) {
            for (int y = 2; y > -4; y--) {
                this.displayPokemons.add(addPokemon(x % 2 == 0 ? "mudkip" : "mimikyu", new Vector3f(x, y, 1f), 0.03f));
            }
        }

        while (!this.window.shouldClose()) {
            this.window.pollEvents();
            update();
            render();
            this.window.swapBuffers();
        }

        this.window.destroy();
    }

    private void update() {
        this.root.update();
        for (RenderObject displayPokemon : this.displayPokemons) {
            displayPokemon.getTransformation().rotate(0.0005f, 0, 1, 0);
        }
    }

    private void render() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        this.renderer.render(this.root, true);
    }

    private void setupRenderer() {
        GL11C.glClearColor(63 / 255f, 191 / 255f, 217 / 255f, 1.0f);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
    }

    private RenderObject addPokemon(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(model.scene, ((GlbReader) model.reader).rawScene));
        pokemon.getTransformation().translate(pos).scale(new Vector3f(scale, scale, scale));

        root.addChild(pokemon);
        return pokemon;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    @Override
    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }
}
