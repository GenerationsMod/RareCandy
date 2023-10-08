package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.renderer.pipeline.Pipeline;

import java.util.function.Function;

public class PipelineRegistry {
    public static Function<String, Pipeline> function;

    public static void setFunction(Function<String, Pipeline> pipelineFunction) {
        function = pipelineFunction;
    }

    public static Pipeline get(String name) {
        return function.apply(name);
    }
}
