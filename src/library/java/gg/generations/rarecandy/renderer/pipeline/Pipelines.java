package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pipelines {
    private static final Pattern LIB_PATTERN = Pattern.compile("#lib:([\\w./-]+)");

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
        return builtin(name, "");
    }

    public static String builtin(String name, String libs) {
        try (var is = Pipelines.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            var source = new String(is.readAllBytes());
            if (libs == null || libs.isBlank()) {
                return source;
            }

            var matcher = LIB_PATTERN.matcher(source);
            var result = new StringBuilder();
            while (matcher.find()) {
                var libName = matcher.group(1);
                var libPath = "/shaders/" + libs + "/" + libName + ".lib.glsl";
                try (var libStream = Pipelines.class.getResourceAsStream(libPath)) {
                    if (libStream == null) {
                        matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                        continue;
                    }

                    matcher.appendReplacement(result, Matcher.quoteReplacement(new String(libStream.readAllBytes())));
                }
            }

            matcher.appendTail(result);
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}
