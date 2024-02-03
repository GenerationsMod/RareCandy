package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.apache.commons.compress.utils.Sets;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoneMesh extends AnimatedMeshObject {
    private static final Set<String> single = Sets.newHashSet("regular");

    public BoneMesh(Map<String, Animation> animations, GLModel model, Skeleton skeleton) {
        super();
        this.animations = animations;
        this.model = model;
        this.skeleton = skeleton;
    }

    @Override
    public <T extends RenderObject> void render(ObjectInstance instances, T object) {
//        if(object instanceof BoneMesh) {
//            for(var instance : instances) {
//
//                var pipeline = PipelineRegistry.get("bone");
//                pipeline.bind(null);
//                pipeline.updateOtherUniforms(instance, object);
//                pipeline.updateTexUniforms(instance, object);
//                model.runDrawCalls();
//                pipeline.unbind(null);
//            }
//        }
    }

    @Override
    public Material getVariant(@Nullable String materialId) {
        return null;
    }

    @Override
    public Set<String> availableVariants() {
        return single;
    }

    public void close() throws IOException {
        super.close();
        model.close();
    }
}
