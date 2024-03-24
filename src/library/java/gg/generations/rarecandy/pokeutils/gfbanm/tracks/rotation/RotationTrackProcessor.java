package gg.generations.rarecandy.pokeutils.gfbanm.tracks.rotation;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.TrackProcesser;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.vector.FixedVectorTrackT;
import org.joml.Quaternionf;

import java.lang.reflect.Type;

public interface RotationTrackProcessor extends TrackProcesser<Quaternionf> {
    class Serializer implements JsonSerializer<RotationTrackProcessor>, JsonDeserializer<RotationTrackProcessor> {
        @Override
        public RotationTrackProcessor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var type = json.getAsJsonObject().remove("type").getAsString();

            return switch (type) {
                case "dynamic" -> context.deserialize(json, DynamicRotationTrackT.class);
                case "fixed" -> context.deserialize(json, FixedRotationTrackT.class);
                case "framed8" -> context.deserialize(json, Framed8RotationTrackT.class);
                case "framed16" -> context.deserialize(json, Framed16RotationTrackT.class);
                default -> null;
            };
        }

        @Override
        public JsonElement serialize(RotationTrackProcessor src, Type typeOfSrc, JsonSerializationContext context) {
            if(src instanceof DynamicRotationTrackT track) return applyType(context, track, "dynamic");
            if(src instanceof FixedRotationTrackT track) return applyType(context, track, "fixed");
            if(src instanceof Framed8RotationTrackT track) return applyType(context, track, "framed8");
            if(src instanceof Framed16RotationTrackT track) return applyType(context, track, "framed16");
            else return null;
        }

        public static <T> JsonObject applyType(JsonSerializationContext context, T t, String type) {
            var obj = context.serialize(t).getAsJsonObject();
            obj.addProperty("type", type);
            return obj;
        }
    }
}
