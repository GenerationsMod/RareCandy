package gg.generations.rarecandy.pokeutils.gfbanm.tracks._float;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.TrackProcesser;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.rotation.*;

import java.lang.reflect.Type;

public interface FloatTrackProcessor extends TrackProcesser<Float> {
    void adjustValue(float value);
    class Serializer implements JsonSerializer<FloatTrackProcessor>, JsonDeserializer<FloatTrackProcessor> {
        @Override
        public FloatTrackProcessor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var type = json.getAsJsonObject().remove("type").getAsString();

            return switch (type) {
                case "dynamic" -> context.deserialize(json, DynamicFloatTrackT.class);
                case "fixed" -> context.deserialize(json, FixedFloatTrackT.class);
                case "framed8" -> context.deserialize(json, Framed8FloatTrackT.class);
                case "framed16" -> context.deserialize(json, Framed16FloatTrackT.class);
                default -> null;
            };
        }

        @Override
        public JsonElement serialize(FloatTrackProcessor src, Type typeOfSrc, JsonSerializationContext context) {
            if(src instanceof DynamicFloatTrackT track) return applyType(context, track, "dynamic");
            if(src instanceof FixedFloatTrackT track) return applyType(context, track, "fixed");
            if(src instanceof Framed8FloatTrackT track) return applyType(context, track, "framed8");
            if(src instanceof Framed16FloatTrackT track) return applyType(context, track, "framed16");
            else return null;
        }

        public static <T> JsonObject applyType(JsonSerializationContext context, T t, String type) {
            var obj = context.serialize(t).getAsJsonObject();
            obj.addProperty("type", type);
            return obj;
        }
    }
}
