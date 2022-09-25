package com.pixelmongenerations.test;

import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.rarecandy.settings.Settings;
import com.pixelmongenerations.rarecandy.settings.TransparencyMethod;
import com.pixelmongenerations.test.tests.InstancingTest;
import com.pixelmongenerations.test.tests.TransparencyFeatureTest;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

public class FeatureTester {
    public static final List<FeatureTest> FEATURE_TESTS = List.of(
            new TransparencyFeatureTest(),
            new InstancingTest()
    );
    public static final Window WINDOW = new Window("RareCandy Feature Test", 1920, 1080);
    public static final Matrix4f PROJECTION_MATRIX = new Matrix4f().perspective((float) Math.toRadians(90), (float) WINDOW.width / WINDOW.height, 0.1f, 1000.0f);
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public final List<FeatureTest> activeFeatures;

    public FeatureTester(List<FeatureTest> activeFeatures) {
        this.activeFeatures = activeFeatures;
        var scene = new RareCandy(new Settings(0, 1, false, TransparencyMethod.NONE, true));
        GL11C.glClearColor(180 / 255f, 210 / 255f, 255 / 255f, 1.0f);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        System.out.println("Loading " + activeFeatures.size() + " Feature Tests");
        for (FeatureTest activeFeature : this.activeFeatures) {
            activeFeature.init(scene, this.viewMatrix);
        }

        double lastFrameTime = 0;
        while (!WINDOW.shouldClose()) {
            WINDOW.pollEvents();
            double frameTime = GLFW.glfwGetTime();
            scene.preRender();
            for (FeatureTest activeFeature : this.activeFeatures) {
                activeFeature.update(scene, frameTime - lastFrameTime);
            }

            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            scene.render(true, false);
            this.WINDOW.swapBuffers();
            lastFrameTime = frameTime;

            GLFW.glfwSetKeyCallback(this.WINDOW.handle, (window1, key, scancode, action, mods) -> {
                if(key == GLFW.GLFW_KEY_Z) {
                    long maxMem = Runtime.getRuntime().maxMemory();
                    long totalMem = Runtime.getRuntime().totalMemory();
                    long freeMem = Runtime.getRuntime().freeMemory();
                    long usedMem = totalMem - freeMem;
                    System.out.printf("Mem: % 2d%% %03d/%03dMB%n", usedMem * 100L / maxMem, usedMem / 1000000, maxMem / 1000000);
                }
            });
        }
        this.WINDOW.destroy();
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
