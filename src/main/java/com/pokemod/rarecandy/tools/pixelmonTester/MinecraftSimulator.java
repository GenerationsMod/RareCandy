package com.pokemod.rarecandy.tools.pixelmonTester;

import com.pokemod.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import java.util.function.Supplier;

public class MinecraftSimulator {
    public static MinecraftSimulator INSTANCE;
    public final Window window;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    public final PokemonTest test;

    public MinecraftSimulator(Supplier<PokemonTest> testSupplier, double sizeMultiplier) {
        INSTANCE = this;
        this.window = new Window("RareCandy Feature Test", (int) (960 * sizeMultiplier), (int) (540 * sizeMultiplier));
        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);
        this.viewMatrix = new Matrix4f().lookAt(0.1f, 0.0f, -1, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        this.test = testSupplier.get();

        GLFW.glfwSetKeyCallback(window.handle, (window1, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_RELEASE) {
                if (key == GLFW.GLFW_KEY_Z) {
                    long maxMem = Runtime.getRuntime().maxMemory();
                    long totalMem = Runtime.getRuntime().totalMemory();
                    long freeMem = Runtime.getRuntime().freeMemory();
                    long usedMem = totalMem - freeMem;
                    System.out.printf("Mem: % 2d%% %03d/%03dMB%n", usedMem * 100L / maxMem, usedMem / 1000000, maxMem / 1000000);
                }

                if (key == GLFW.GLFW_KEY_LEFT) test.leftTap();
                if (key == GLFW.GLFW_KEY_RIGHT) test.rightTap();
                if (key == GLFW.GLFW_KEY_SPACE) test.space();
            }
        });

        var scene = new RareCandy();
        GL11C.glClearColor(0f, 0f, 0f, 1);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        //GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        test.init(scene, projectionMatrix, viewMatrix);

        while (!window.shouldClose()) {
            test.loop();
            window.pollEvents();
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            test.update();
            scene.render(false, PokemonTest.getTimePassed());
            window.swapBuffers();
        }

        window.destroy();
        scene.close();
    }

    public static void main(String[] args) {
        new MinecraftSimulator(() -> new PokemonTest(args), args.length >= 2 ? Double.parseDouble(args[1]) : 1);
    }
}
