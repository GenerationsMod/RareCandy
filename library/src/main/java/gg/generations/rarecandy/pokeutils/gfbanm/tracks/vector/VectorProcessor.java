package gg.generations.rarecandy.pokeutils.gfbanm.tracks.vector;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.TrackProcesser;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.rotation.*;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public interface VectorProcessor extends TrackProcesser<Vector3f> {
    class Serializer implements JsonSerializer<VectorProcessor>, JsonDeserializer<VectorProcessor> {
        @Override
        public VectorProcessor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var type = json.getAsJsonObject().remove("type").getAsString();

            return switch (type) {
                case "dynamic" -> context.deserialize(json, DynamicVectorTrackT.class);
                case "fixed" -> context.deserialize(json, FixedVectorTrackT.class);
                case "framed8" -> context.deserialize(json, Framed8VectorTrackT.class);
                case "framed16" -> context.deserialize(json, Framed16VectorTrackT.class);
                default -> null;
            };
        }

        @Override
        public JsonElement serialize(VectorProcessor src, Type typeOfSrc, JsonSerializationContext context) {
            if(src instanceof DynamicVectorTrackT track) return applyType(context, track, "dynamic");
            if(src instanceof FixedVectorTrackT track) return applyType(context, track, "fixed");
            if(src instanceof Framed8VectorTrackT track) return applyType(context, track, "framed8");
            if(src instanceof Framed16VectorTrackT track) return applyType(context, track, "framed16");
            else return null;
        }

        public static <T> JsonObject applyType(JsonSerializationContext context, T t, String type) {
            var obj = context.serialize(t).getAsJsonObject();
            obj.addProperty("type", type);
            return obj;
        }
    }
}
