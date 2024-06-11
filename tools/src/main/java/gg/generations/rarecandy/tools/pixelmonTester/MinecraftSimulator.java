package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

public class MinecraftSimulator {
    private static final double START_TIME = System.currentTimeMillis();
    public final Window window;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public final PokemonTest test;
    private final ModelLoader loader = new ModelLoader();

    public MinecraftSimulator(PokemonTest test, int sizeMultiplier) {
        this.test = test;
        this.window = new Window("RareCandy Feature Test", 960 * sizeMultiplier, 540 * sizeMultiplier);
        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);

        GLFW.glfwSetKeyCallback(window.handle, (window1, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_Z) {
                long maxMem = Runtime.getRuntime().maxMemory();
                long totalMem = Runtime.getRuntime().totalMemory();
                long freeMem = Runtime.getRuntime().freeMemory();
                long usedMem = totalMem - freeMem;
                printError("Mem: % 2d%% %03d/%03dMB%n".formatted(usedMem * 100L / maxMem, usedMem / 1000000, maxMem / 1000000));
            }

            if (action == GLFW.GLFW_RELEASE) {
                if (key == GLFW.GLFW_KEY_LEFT) test.leftTap();
                if (key == GLFW.GLFW_KEY_RIGHT) test.rightTap();
                if (key == GLFW.GLFW_KEY_SPACE) test.space();
            }
        });

        var scene = new RareCandy();
        GL11C.glClearColor(0.5f, 0.5f, 0.5f, 1);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        test.init(scene, projectionMatrix, viewMatrix);

        while (!window.shouldClose()) {
            window.pollEvents();
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

            var timePassed = (System.currentTimeMillis() - START_TIME);

            scene.render(false, timePassed);
            window.swapBuffers();
        }

        window.destroy();
        loader.close();
    }

    public static void main(String[] args) {
        new MinecraftSimulator(new PokemonTest(args), args.length >= 2 ? Integer.parseInt(args[1]) : 1);
    }
}
