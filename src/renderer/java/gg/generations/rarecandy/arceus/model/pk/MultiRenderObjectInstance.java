package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.legacy.animation.AnimationController;
import gg.generations.rarecandy.legacy.animation.AnimationInstance;
import gg.generationsmod.rarecandy.model.animation.Animation;
import gg.generationsmod.rarecandy.model.animation.Transform;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiRenderObjectInstance<T extends MultiRenderObject<?>> {
    private final T object;
    private final Matrix4f transform;

    private final List<MultiRenderingInstance<?>> proxies;
    private String variant;

    private String materialName;
    private RareCandyScene<RenderingInstance> scene;

    public AnimationInstance currentAnimation;

    public MultiRenderObjectInstance(T object, Matrix4f transform) {
        this(object, transform.scale(object.getScale()), "");
    }

    public MultiRenderObjectInstance(T object, Matrix4f transform, String varaint) {
        this.object = object;
        this.transform = transform;
        this.variant = varaint;
        this.proxies = new ArrayList<>();

        object.meshes.forEach((key, value) -> {

            proxies.add(createInstance(key, value, () -> object.getMapForVariants(this.getVariant()).get(key), transform));
        });
    }

    protected MultiRenderingInstance<?> createInstance(String name, Model model, Supplier<PkMaterial> materialSupplier, Matrix4f transform) {
        return new MultiRenderingInstance<>(name, model, this, materialSupplier, transform);
    }

    public void addToScene(RareCandyScene<RenderingInstance> scene) {
        var hide = object.shouldHide(variant);

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

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        var hideNew = object.shouldHide(variant);

        if(scene != null) {

            proxies.stream().filter(a -> !a.isChanging()).forEach(instance -> {

                instance.setChanging();
                scene.removeInstance(instance);
                if (!hideNew.contains(instance.getName())) scene.addInstance(instance);

            });
        }
        this.variant = variant == null ? "" : variant;
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

}
