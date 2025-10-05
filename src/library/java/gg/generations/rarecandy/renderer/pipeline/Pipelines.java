package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;

import java.io.IOException;

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

    public static String builtin(String name) {
        try (var is = Pipelines.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}
