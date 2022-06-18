package com.pixelmongenerations.test;

import com.pixelmongenerations.pixelmonassetutils.PixelAsset;
import com.pixelmongenerations.pixelmonassetutils.reader.GlbReader;
import com.pixelmongenerations.rarecandy.AnimationUtil;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.components.StaticRenderObject;
import com.pixelmongenerations.rarecandy.rendering.CompatibilityProvider;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RenderScene;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

public class StressTest implements CompatibilityProvider {

    public final Window window = new Window("Pixelmon Renderer Stress Test", 1920, 1080);
    public final Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

    public static void main(String[] args) {
        new StressTest();
    }

    public StressTest() {
        GL11C.glClearColor(63 / 255f, 191 / 255f, 217 / 255f, 1.0f);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        List<RenderObject> models = Stream.of("bulbasaur", "charmander", "clefairy", "diglet", "ditto", "mimikyu", "mudkip", "solosis").map(this::addPokemon).toList();
        Random random = new Random();

        int pokemonRendered = 0;
        RenderScene scene = new RenderScene(this);
        for (int z = 1; z < 64; z++) {
            for (int x = -5; x < 10; x++) {
                for (int y = 2; y > -4; y--) {
                    InstanceState instance = new InstanceState(new Matrix4f(), getViewMatrix());
                    RenderObject model = models.get(random.nextInt(models.size()));
                    instance.transformationMatrix.translate(new Vector3f(x, y, z)).scale(new Vector3f(0.02f, 0.02f, 0.02f));
                    scene.add(model, instance);
                    pokemonRendered++;
                }
            }
        }
        System.out.println("Models On Screen: " + pokemonRendered);

        // For Water Testing
        RenderObject pcModel = loadStaticModel("pc");

        while (!this.window.shouldClose()) {
            this.window.pollEvents();
            scene.preRender();
            for (InstanceState object : scene.getAllInstances()) {
                object.transformationMatrix.rotate(0.005f, 0, 1, 0);
            }

            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            scene.render(true, false);
            this.window.swapBuffers();

/*            long i = Runtime.getRuntime().maxMemory();
            long j = Runtime.getRuntime().totalMemory();
            long k = Runtime.getRuntime().freeMemory();
            long l = j - k;
            System.out.printf("Mem: % 2d%% %03d/%03dMB%n", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i));*/
        }
        this.window.destroy();
    }

    private static long bytesToMegabytes(long bytes) {
        return bytes / 1024L / 1024L;
    }

    private RenderObject loadStaticModel(String name) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(StressTest.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        return StaticRenderObject.loadStaticFile(model.scene, ((GlbReader) model.reader).rawScene, 0);
    }

    private RenderObject addPokemon(String name) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(StressTest.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        return AnimationUtil.loadAnimatedFile(model.scene, ((GlbReader) model.reader).rawScene);
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }
}
