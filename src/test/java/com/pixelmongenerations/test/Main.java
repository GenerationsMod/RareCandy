package com.pixelmongenerations.test;

import com.pixelmongenerations.pixelmonassetutils.PixelAsset;
import com.pixelmongenerations.pixelmonassetutils.reader.GlbReader;
import com.pixelmongenerations.rarecandy.AnimationUtil;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.components.StaticRenderObject;
import com.pixelmongenerations.rarecandy.rendering.CompatibilityProvider;
import com.pixelmongenerations.rarecandy.rendering.RenderScene;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Main implements CompatibilityProvider {

    public final Window window = new Window("Pixelmon Renderer Test", 1920, 1080);
    public final Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    private final RenderScene scene = new RenderScene(this);

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        if (Files.exists(Paths.get("C:/Program Files/RenderDoc/renderdoc.dll"))) {
            System.load("C:/Program Files/RenderDoc/renderdoc.dll");
        }

        GL11C.glClearColor(63 / 255f, 191 / 255f, 217 / 255f, 1.0f);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        for (int x = -5; x < 10; x++) {
            for (int y = 2; y > -4; y--) {
                addPokemon(x % 2 == 0 ? "mudkip" : "mimikyu", new Vector3f(x, y, 1f), x % 2 == 0 ? 0.03f : 0.02f);
            }
        }

        // For Water Testing
        loadStaticModel("pc", new Vector3f(), 0.6f);

        while (!this.window.shouldClose()) {
            this.window.pollEvents();
            this.scene.preRender();
            for (RenderObject object : this.scene.getObjects()) {
                object.getTransformationMatrix().rotate(0.005f, 0, 1, 1);
            }

            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            this.scene.render(true);
            this.window.swapBuffers();
        }
        this.window.destroy();
    }

    private void loadStaticModel(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject object = StaticRenderObject.loadStaticFile(model.scene, ((GlbReader) model.reader).rawScene, 0);
        object.getTransformationMatrix().translate(pos).rotate((float) Math.toRadians(90), new Vector3f(1, 0, 0)).scale(new Vector3f(scale, scale, scale));

        this.scene.add(object);
    }

    private void addPokemon(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = AnimationUtil.loadAnimatedFile(model.scene, ((GlbReader) model.reader).rawScene);
        pokemon.getTransformationMatrix().translate(pos).scale(new Vector3f(scale, scale, scale));

        this.scene.add(pokemon);
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
