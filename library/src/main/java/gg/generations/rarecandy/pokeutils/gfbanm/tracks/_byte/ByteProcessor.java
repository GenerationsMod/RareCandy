package gg.generations.rarecandy.pokeutils.gfbanm.tracks._byte;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.TrackProcesser;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks._float.*;

import java.lang.reflect.Type;

public interface ByteProcessor extends TrackProcesser<Byte> {
    class Serializer implements JsonSerializer<ByteProcessor>, JsonDeserializer<ByteProcessor> {
        @Override
        public ByteProcessor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var type = json.getAsJsonObject().remove("type").getAsString();

            return switch (type) {
                case "dynamic" -> context.deserialize(json, DynamicByteTrackT.class);
                case "fixed" -> context.deserialize(json, FixedByteTrackT.class);
                case "framed8" -> context.deserialize(json, Framed8ByteTrackT.class);
                case "framed16" -> context.deserialize(json, Framed16ByteTrackT.class);
                default -> null;
            };
        }

        @Override
        public JsonElement serialize(ByteProcessor src, Type typeOfSrc, JsonSerializationContext context) {
            if(src instanceof DynamicByteTrackT track) return applyType(context, track, "dynamic");
            if(src instanceof FixedByteTrackT track) return applyType(context, track, "fixed");
            if(src instanceof Framed8ByteTrackT track) return applyType(context, track, "framed8");
            if(src instanceof Framed16ByteTrackT track) return applyType(context, track, "framed16");
            else return null;
        }

        public static <T> JsonObject applyType(JsonSerializationContext context, T t, String type) {
            var obj = context.serialize(t).getAsJsonObject();
            obj.addProperty("type", type);
            return obj;
        }
    }
}
