package com.pokemod.test;

import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.test.tests.*;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class FeatureTester {
    public static final List<FeatureTest> FEATURE_TESTS = List.of(
            new AnimationTest(),
            new GuiTest(),
            new StatUpTest()
    );
    private static final double startTime = System.currentTimeMillis();
    public static final Window WINDOW = new Window("RareCandy Feature Test", 960, 540);
    public static final Matrix4f PROJECTION_MATRIX = new Matrix4f().perspective((float) Math.toRadians(90), (float) WINDOW.width / WINDOW.height, 0.1f, 1000.0f);
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public final List<FeatureTest> activeFeatures;

    public FeatureTester(List<FeatureTest> activeFeatures) {
        GLFW.glfwSetKeyCallback(WINDOW.handle, (window1, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_Z) {
                long maxMem = Runtime.getRuntime().maxMemory();
                long totalMem = Runtime.getRuntime().totalMemory();
                long freeMem = Runtime.getRuntime().freeMemory();
                long usedMem = totalMem - freeMem;
                System.out.printf("Mem: % 2d%% %03d/%03dMB%n", usedMem * 100L / maxMem, usedMem / 1000000, maxMem / 1000000);
            }

            if(action == GLFW_RELEASE) {
                if(key == GLFW_KEY_LEFT) {
                    this.leftTap();
                }

                if(key == GLFW_KEY_RIGHT) {
                    this.rightTap();
                }
            }
        });

        this.activeFeatures = activeFeatures;
        var scene = new RareCandy();
        GL11C.glClearColor(0.5f, 0.5f, 0.5f, 1);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        Pipelines.onInitialize();
        for (var activeFeature : this.activeFeatures) activeFeature.init(scene, this.viewMatrix);

        var lastFrameTime = 0d;
        while (!WINDOW.shouldClose()) {
            WINDOW.pollEvents();
            var frameTime = GLFW.glfwGetTime();
            for (var activeFeature : this.activeFeatures) {
                activeFeature.update(scene, frameTime - lastFrameTime);
            }

            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            scene.render(false, ((System.currentTimeMillis() - startTime) / 16000));
            WINDOW.swapBuffers();
            lastFrameTime = frameTime;
        }

        WINDOW.destroy();
        scene.close();
    }

    private void leftTap() {
        activeFeatures.forEach(FeatureTest::leftTap);
    }

    private void rightTap() {
        activeFeatures.forEach(FeatureTest::rightTap);
    }

    public static void main(String[] args) {
        var activeTests = new ArrayList<FeatureTest>();
        if (args.length > 0) {
            for (String arg : args) {
                for (FeatureTest featureTest : FEATURE_TESTS) {
                    if (featureTest.id.equals(arg)) {
                        activeTests.add(featureTest);
                    }
                }
            }
        } else {
            activeTests.addAll(FEATURE_TESTS);
        }
        new FeatureTester(activeTests);
    }
}
