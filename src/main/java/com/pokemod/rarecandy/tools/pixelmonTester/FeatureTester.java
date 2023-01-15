package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

public class FeatureTester {
    private static final double START_TIME = System.currentTimeMillis();
    public final Window window;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public final FeatureTest test;

    public FeatureTester(FeatureTest test, int sizeMultiplier) {
        this.test = test;
        this.window = new Window("RareCandy Feature Test", 960 * sizeMultiplier, 540 * sizeMultiplier);
        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);

        GLFW.glfwSetKeyCallback(window.handle, (window1, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_Z) {
                long maxMem = Runtime.getRuntime().maxMemory();
                long totalMem = Runtime.getRuntime().totalMemory();
                long freeMem = Runtime.getRuntime().freeMemory();
                long usedMem = totalMem - freeMem;
                System.out.printf("Mem: % 2d%% %03d/%03dMB%n", usedMem * 100L / maxMem, usedMem / 1000000, maxMem / 1000000);
            }

            if (action == GLFW.GLFW_RELEASE) {
                if (key == GLFW.GLFW_KEY_LEFT) test.leftTap();
                if (key == GLFW.GLFW_KEY_RIGHT) test.rightTap();
            }
        });

        var scene = new RareCandy();
        GL11C.glClearColor(0.5f, 0.5f, 0.5f, 1);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        test.init(scene, projectionMatrix, viewMatrix);

        var lastFrameTime = 0d;
        while (!window.shouldClose()) {
            window.pollEvents();
            var frameTime = GLFW.glfwGetTime();
            test.update(scene, frameTime - lastFrameTime);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            scene.render(false, ((System.currentTimeMillis() - START_TIME) / 16000));
            window.swapBuffers();
            lastFrameTime = frameTime;
        }

        window.destroy();
        scene.close();
    }

    public static void main(String[] args) {
        new FeatureTester(new PokemonTest(args), args.length >= 2 ? Integer.parseInt(args[1]) : 1);
    }
}
