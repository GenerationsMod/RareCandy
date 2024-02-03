package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class ObjectManager {
    private final AnimationController animationController = new AnimationController();
    private final List<Tri<RenderObject, ObjectInstance, Boolean>> objects = new ArrayList<>();

    public void update(double secondsPassed) {
        if(objects.isEmpty()) return;
        for (var pair : objects)
            if (pair.t1() instanceof AnimatedObjectInstance animatedObjectInstance)
                if (animatedObjectInstance.currentAnimation != null)
                    if (!animationController.playingInstances.contains((animatedObjectInstance.currentAnimation)))
                        animationController.playingInstances.add(animatedObjectInstance.currentAnimation);

        animationController.render(secondsPassed);
    }

    private static Vector3f DUMMY_1 = new Vector3f();
    private static Vector3f DUMMY_2 = new Vector3f();

    public void render(Matrix4f cameraMatrix) {
        cameraMatrix.getTranslation(DUMMY_1);

        update();

        objects.sort(Comparator.<Tri<RenderObject, ObjectInstance, Boolean>, Boolean>comparing(Tri::t2).thenComparingDouble(value -> {
            value.t1.transformationMatrix().getTranslation(DUMMY_2);
            return DUMMY_1.distance(DUMMY_2);
        }));

        for (var entry : objects) {
            var object = entry.t();

            if (object.isReady() && !entry.t2) {
                object.render(entry.t1());
            }
        }


        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        for (var entry : objects) {
            var object = entry.t();

            if (object.isReady() && entry.t2) {
                object.render(entry.t1());
            }
        }

        GL11.glDepthFunc(GL11.GL_LESS);
    }

    public void update() {
        for (var entry : objects) {
            var object = entry.t();

            if (object.isReady()) {
                object.update();
            }
        }
    }

    public <T extends ObjectInstance> T add(@NotNull RenderObject object, @NotNull T instance) {
        instance.link(object);
        objects.add(new Tri<>(object, instance, object.getMaterial(instance.materialId()).blendType() != BlendType.None));
        return instance;
    }

    /**
     * Used within Minecraft to provide easier support for the rendering style they use
     */
    public void clearObjects() {
        objects.clear();
    }

    public record Tri<T, T1, T2>(T t, T1 t1, T2 t2) {
    }
}
