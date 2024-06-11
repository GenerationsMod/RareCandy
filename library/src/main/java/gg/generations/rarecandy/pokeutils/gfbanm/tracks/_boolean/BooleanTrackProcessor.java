package gg.generations.rarecandy.pokeutils.gfbanm.tracks._boolean;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.TrackProcesser;

import java.lang.reflect.Type;

public interface BooleanTrackProcessor extends TrackProcesser<Boolean> {
    class Serializer implements JsonSerializer<BooleanTrackProcessor>, JsonDeserializer<BooleanTrackProcessor> {
        @Override
        public BooleanTrackProcessor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var type = json.getAsJsonObject().remove("type").getAsString();

            return switch (type) {
                case "dynamic" -> context.deserialize(json, DynamicBooleanTrackT.class);
                case "fixed" -> context.deserialize(json, FixedBooleanTrackT.class);
                case "framed8" -> context.deserialize(json, Framed8BooleanTrackT.class);
                case "framed16" -> context.deserialize(json, Framed16BooleanTrackT.class);
                default -> null;
            };
        }

        @Override
        public JsonElement serialize(BooleanTrackProcessor src, Type typeOfSrc, JsonSerializationContext context) {
            if(src instanceof DynamicBooleanTrackT track) return applyType(context, track, "dynamic");
            if(src instanceof FixedBooleanTrackT track) return applyType(context, track, "fixed");
            if(src instanceof Framed8BooleanTrackT track) return applyType(context, track, "framed8");
            if(src instanceof Framed16BooleanTrackT track) return applyType(context, track, "framed16");
            else return null;
        }

        public static <T> JsonObject applyType(JsonSerializationContext context, T t, String type) {
            var obj = context.serialize(t).getAsJsonObject();
            obj.addProperty("type", type);
            return obj;
        }
    }
}
