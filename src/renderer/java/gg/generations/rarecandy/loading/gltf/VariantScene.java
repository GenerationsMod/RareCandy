package gg.generations.rarecandy.loading.gltf;

import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.RenderingInstance;

public class VariantScene extends RareCandyScene<VariantInstance.InstanceProxy> {
    public void addVariantInstance(VariantInstance instance) {
        instance.instances().forEach(this::addInstance);
    }

    public void removeVariantInstance(VariantInstance instance) {
        instance.instances().forEach(this::removeInstance);
    }
}
