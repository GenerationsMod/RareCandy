package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

//TODO: make room for future options for modifying meshes.
public record MeshOptions(boolean invert, List<String> aliases) implements IMeshOptions {
}

