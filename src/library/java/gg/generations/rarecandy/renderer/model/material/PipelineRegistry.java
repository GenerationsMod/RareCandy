package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;

import java.util.function.Function;

public class PipelineRegistry {
    public static Function<String, TraditionalPipeline> function;

    public static void setFunction(Function<String, TraditionalPipeline> pipelineFunction) {
        function = pipelineFunction;
    }

    public static TraditionalPipeline get(String name) {
        return function.apply(name);
    }
}
