package gg.generations.rarecandy.renderer.animation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import org.joml.Vector2f;

import javax.xml.crypto.dsig.TransformService;

public record TransformSet(
        ITransform diffuse,
        ITransform layer,
        ITransform mask,
        ITransform emission
) implements ITransformSet {}