package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

public class FeatureTester {
    private static final double startTime = System.currentTimeMillis();
    public static final Window WINDOW = new Window("RareCandy Feature Test", 960, 540);
    public static final Matrix4f PROJECTION_MATRIX = new Matrix4f().perspective((float) Math.toRadians(90), (float) WINDOW.width / WINDOW.height, 0.1f, 1000.0f);
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public final FeatureTest test;

    public FeatureTester(FeatureTest test) {
        this.test = test;

        GLFW.glfwSetKeyCallback(WINDOW.handle, (window1, key, scancode, action, mods) -> {
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

        Pipelines.onInitialize();
        test.init(scene, this.viewMatrix);

        var lastFrameTime = 0d;
        while (!WINDOW.shouldClose()) {
            WINDOW.pollEvents();
            var frameTime = GLFW.glfwGetTime();
            test.update(scene, frameTime - lastFrameTime);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            scene.render(false, ((System.currentTimeMillis() - startTime) / 16000));
            WINDOW.swapBuffers();
            lastFrameTime = frameTime;
        }

        WINDOW.destroy();
        scene.close();
    }

    public static void main(String[] args) {
        new FeatureTester(new PokemonTest(args));
    }
}
