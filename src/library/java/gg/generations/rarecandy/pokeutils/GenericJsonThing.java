package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

public record GenericJsonThing<T>(BiFunction<T, JsonSerializationContext, JsonElement> serializer,
                                  BiFunction<JsonElement, JsonDeserializationContext, T> deserializer) implements JsonSerializer<T>, JsonDeserializer<T> {


    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserializer.apply(json, context);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return serializer.apply(src, context);
    }
}
