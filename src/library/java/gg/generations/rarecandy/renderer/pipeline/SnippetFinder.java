package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.pokeutils.resource.ResourceReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SnippetFinder {
    private Map<String, String> entries = new HashMap<>();

    private final ResourceReader reader;

    private SnippetFinder(ResourceReader reader) {
        this.reader = reader;
    }

    public static SnippetFinder create(ResourceReader reader) {
        return new SnippetFinder(reader);
    }

    public SnippetFinder addSnippet(String group, String id) {
        entries.put("#" + group + ":" + id, id + ".lib.glsl");
        return this;
    }

    public Map<String, String> compile() {
        var map = new HashMap<String, String>();

        entries.forEach((key, value) -> {
            try {
                map.put(key, reader.readAsString(value));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return map;
    }

}
