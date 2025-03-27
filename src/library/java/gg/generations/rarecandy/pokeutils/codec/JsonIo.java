package gg.generations.rarecandy.pokeutils.codec;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import gg.generations.rarecandy.pokeutils.ModelConfig;

public class JsonIo {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    public static <A> A read(Codec<A> codec, byte[] bytes) {
        return codec.parse(JsonOps.INSTANCE, GSON.fromJson(new String(bytes), JsonElement.class)).result().get();
    }

    public static byte[] write(Codec<ModelConfig> codec, ModelConfig config) {
        var element = codec.encodeStart(JsonOps.INSTANCE, config).result().orElse(new JsonObject());

        return GSON.toJson(element).getBytes();
    }
}
