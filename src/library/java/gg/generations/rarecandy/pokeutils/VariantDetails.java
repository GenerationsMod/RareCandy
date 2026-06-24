package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import gg.generations.rarecandy.renderer.animation.ITransformSet;
import gg.generations.rarecandy.renderer.animation.TransformSet;

public record VariantDetails(String material, String effect, Boolean paradox, Boolean hide, ITransformSet transform) implements IVariantDetails { }


