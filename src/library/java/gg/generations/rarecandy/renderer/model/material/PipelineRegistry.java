package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Function;

public class PipelineRegistry {
    public static TriFunction<Material, ObjectInstance, RenderObject, Pipeline> function;

    public static void setFunction(TriFunction<Material, ObjectInstance, RenderObject, Pipeline> pipelineFunction) {
        function = pipelineFunction;
    }

    public static Pipeline get(Material material, ObjectInstance instance, RenderObject object) {
        return function.apply(material, instance, object);
    }

}
