package gg.generations.rarecandy.loading.gltf;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.legacy.model.misc.Material;
import org.joml.Matrix4f;

import java.util.List;

public class VariantInstance {
    private final List<InstanceProxy> instanceProxies;
    private String variant;
    private final Matrix4f transform;

    public VariantInstance(List<VariantModel.ModelSet> modelSets) {
        instanceProxies = modelSets.stream().map(InstanceProxy::new).toList();
        transform = new Matrix4f();
    }

    public class InstanceProxy implements RenderingInstance {
        private final VariantModel.ModelSet set;

        public InstanceProxy(VariantModel.ModelSet set) {
            this.set = set;
        }

        @Override
        public Model getModel() {
            return set.model();
        }

        @Override
        public Material getMaterial() {
            return set.materialMap().get(VariantInstance.this.variant != null ? VariantInstance.this.variant : "default");
        }

        @Override
        public Matrix4f getTransform() {
            return VariantInstance.this.transform;
        }

        @Override
        public boolean visible() {
            return set.visibilityMap().get(VariantInstance.this.variant != null ? VariantInstance.this.variant : "default");
        }
    }

    public List<InstanceProxy> instances() {
        return instanceProxies;
    }
}
