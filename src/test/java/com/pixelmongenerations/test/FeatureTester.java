package com.pixelmongenerations.test;

import com.pixelmongenerations.rarecandy.rendering.CompatibilityProvider;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.RenderScene;
import com.pixelmongenerations.test.tests.InstancingTest;
import com.pixelmongenerations.test.tests.TransparencyFeatureTest;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

public class FeatureTester implements CompatibilityProvider {
    public static final List<FeatureTest> FEATURE_TESTS = List.of(
            new TransparencyFeatureTest(),
            new InstancingTest()
    );
    public final Window window = new Window("RareCandy Feature Test", 1920, 1080);
    public final Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public final List<FeatureTest> activeFeatures;

    public FeatureTester(List<FeatureTest> activeFeatures) {
        this.activeFeatures = activeFeatures;
        RenderScene scene = new RenderScene(this);
        GL11C.glClearColor(180 / 255f, 210 / 255f, 255 / 255f, 1.0f);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        System.out.println("Loading " + activeFeatures.size() + " Feature Tests");
        for (FeatureTest activeFeature : this.activeFeatures) {
            activeFeature.init(scene, this.viewMatrix);
        }

        while (!this.window.shouldClose()) {
            this.window.pollEvents();
            scene.preRender();
            for (FeatureTest activeFeature : this.activeFeatures) {
                activeFeature.update(scene);
            }

            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            scene.render(true, false);
            this.window.swapBuffers();

/*          long i = Runtime.getRuntime().maxMemory();
            long j = Runtime.getRuntime().totalMemory();
            long k = Runtime.getRuntime().freeMemory();
            long l = j - k;
            System.out.printf("Mem: % 2d%% %03d/%03dMB%n", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i));*/
        }
        this.window.destroy();
    }

    public static void main(String[] args) {
        List<FeatureTest> activeTests = new ArrayList<>();
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

    @Override
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }
}
