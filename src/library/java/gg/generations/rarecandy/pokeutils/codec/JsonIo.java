package gg.generations.rarecandy.pokeutils.codec;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.Optional;

public class JsonIo {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    public static <A> A read(Codec<A> codec, byte[] bytes) {
        return read(codec, new String(bytes));
    }

    public static <A> A read(Codec<A> codec, String bytes) {
        return codec.parse(JsonOps.INSTANCE, GSON.fromJson(bytes, JsonElement.class)).result().get();
    }

    public static <T> Optional<JsonElement> writeJson(Codec<T> codec, T input) {
        return codec.encodeStart(JsonOps.INSTANCE, input).result();
    }


    public static <T> Optional<String> write(Codec<T> codec, T config) {
        return writeJson(codec, config).map(GSON::toJson);
    }

    public static <T> Optional<byte[]> writeAsBytes(Codec<T> codec, T config) {
        return write(codec, config).map(String::getBytes);
    }
}
