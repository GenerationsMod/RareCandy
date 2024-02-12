package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

import java.util.function.Function;

public class PipelineRegistry {
    public static Function<String, ShaderProgram> function;

    public static void setFunction(Function<String, ShaderProgram> pipelineFunction) {
        function = pipelineFunction;
    }

    public static ShaderProgram get(String name) {
        return function.apply(name);
    }
}
