package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;

public class Pipelines {
    public static TraditionalPipeline.Builder traditional(String vertSource, String fragSource) {
        return TraditionalPipeline.builder(vertSource, fragSource);
    }

    public static TraditionalPipeline.Builder traditional(String vertSource, String geomSource, String fragSource) {
        return TraditionalPipeline.builder(vertSource, geomSource, fragSource);
    }

    public static ComputePipeline.Builder compute(String computeSource) {
        return ComputePipeline.builder(computeSource);
    }
}
