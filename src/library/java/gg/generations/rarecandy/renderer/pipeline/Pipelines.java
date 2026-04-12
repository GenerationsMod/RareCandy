package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        var shaderPath = resourcePath("shaders", name);
        try (var is = Pipelines.class.getResourceAsStream(shaderPath)) {
            if (is == null) {
                throw new IllegalArgumentException("Built in shader not found: " + shaderPath);
            }

            var source = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            if (libs == null || libs.isBlank()) {
                return source;
            }

            var matcher = LIB_PATTERN.matcher(source);
            var result = new StringBuilder();
            while (matcher.find()) {
                var libName = matcher.group(1);
                var libPath = resourcePath("shaders", libs, libName + ".lib.glsl");
                try (var libStream = Pipelines.class.getResourceAsStream(libPath)) {
                    if (libStream == null) {
                        throw new IllegalArgumentException("Built in shader library not found: " + libPath + " referenced from " + shaderPath);
                    }

                    matcher.appendReplacement(result, Matcher.quoteReplacement(new String(libStream.readAllBytes(), StandardCharsets.UTF_8)));
                }
            }

            matcher.appendTail(result);
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }

    private static String resourcePath(String... parts) {
        var joined = new StringBuilder();
        for (var part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }

            var normalized = part.replace('\\', '/');
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            while (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }

            if (normalized.isBlank()) {
                continue;
            }

            if (!joined.isEmpty()) {
                joined.append('/');
            }
            joined.append(normalized);
        }

        return "/" + joined;
    }
}
