package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.pokeutils.resource.JarResourceReader;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ShaderSource {
    private Map<String, String> snippets = new HashMap<>();

    private ShaderSource() {}

    public static ShaderSource create() {
        return new ShaderSource();
    }

    public ShaderSource finder(SnippetFinder finder) {
        finder.compile().forEach(snippets::putIfAbsent);

        return this;
    }

    public String compile(String src) {
        return compile(ResourceReader.DummyReader.INSTANCE, src);
    }

    public String compile(ResourceReader reader, String name) {
//        if(!reader.hasFile(name)) return null;

        try {
            var compiledSource = reader.readAsString(name);

            for(var entry : snippets.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                compiledSource = compiledSource.replaceAll(key, value);
            }

            return compiledSource;
        } catch (IOException e) {
            return null;
        }
    }

    public ProgramSet compileSet(ResourceReader reader, String name) {
        var vertex = compile(reader, name + ".vs.glsl");
        var geometry = compile(reader, name + ".gs.glsl");
        var fragment = compile(reader, name + ".fs.glsl");
        var compute = compile(reader, name + ".cs.glsl");
        return new ProgramSet(vertex, geometry, fragment, compute);
    }
}
