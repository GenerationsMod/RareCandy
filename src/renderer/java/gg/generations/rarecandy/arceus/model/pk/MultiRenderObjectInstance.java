package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.Material;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.legacy.animation.AnimationController;
import gg.generations.rarecandy.legacy.animation.AnimationInstance;
import gg.generationsmod.rarecandy.model.animation.Animation;
import gg.generationsmod.rarecandy.model.animation.Transform;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiRenderObjectInstance<T extends MultiRenderObject<?>> implements RenderingInstance {
    private T object;
    private final Matrix4f transform;

    private String variant;

    private Map<String, PkMaterial> materials = new HashMap<>();
    private RareCandyScene<RenderingInstance> scene;

    public AnimationInstance currentAnimation;
    private List<String> hidden;

    public MultiRenderObjectInstance(T object, Matrix4f transform) {
        this(object, transform.scale(object.getScale()), "");
    }

    public MultiRenderObjectInstance(T object, Matrix4f transform, String varaint) {
        this.object = object;
        this.transform = transform;
        this.variant = varaint;
        updateMaterials();
    }

    public void addToScene(RareCandyScene<RenderingInstance> scene) {
        hidden = object.shouldHide(variant);



        if (this.scene != null && this.scene != scene) {
            proxies.stream().filter(a -> !hide.contains(a.getName())).forEach(instance -> {
                this.scene.removeInstance(instance);
                scene.addInstance(instance);
            });
            this.scene = scene;

        } else {
            this.scene = scene;

            proxies.stream().filter(a -> !hide.contains(a.getName())).forEach(instance -> this.scene.addInstance(instance));
        }
    }

    public void setObject(T object) {
        this.object = object;
        updateMaterials();
    }

    private void updateMaterials() {
        materials.clear();
        materials = object.getMapForVariants(getVariant());
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant == null ? "" : variant;
        materials = object.getMapForVariants(getVariant());
        hidden = object.shouldHide(variant);
    }

    public void removeFromScene() {
        if (this.scene != null) {
            proxies.forEach(this.scene::removeInstance);
            this.scene = null;
        }
    }

    public Map<String, Animation<?>> getAnimationsIfAvailable() {
        return object.animations;
    }

    public Matrix4f[] getTransforms() {
        if (currentAnimation == null || currentAnimation.matrixTransforms == null)
            return AnimationController.NO_ANIMATION;
        return currentAnimation.matrixTransforms;
    }

    public void changeAnimation(AnimationInstance newAnimation) {
        if (currentAnimation != null) currentAnimation.destroy();
        this.currentAnimation = newAnimation;
    }

    public Transform getOffset(String material) {
        return currentAnimation != null ? currentAnimation.getOffset(material) : AnimationController.NO_OFFSET;
    }

    public MultiRenderObject<?> object() {
        return object;
    }

    @Override
    public Model getModel() {
        return null;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public Matrix4f getTransform() {
        return null;
    }
}
