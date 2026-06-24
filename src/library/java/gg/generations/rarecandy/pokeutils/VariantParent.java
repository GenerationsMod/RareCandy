package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public record VariantParent(String inherits, Map<String, IVariantDetails> details) implements IVariantParent { }