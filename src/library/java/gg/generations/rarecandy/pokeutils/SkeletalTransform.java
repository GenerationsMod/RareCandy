package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record SkeletalTransform(Vector3f position, Quaternionf rotation) implements ISkeletalTransform {
}
