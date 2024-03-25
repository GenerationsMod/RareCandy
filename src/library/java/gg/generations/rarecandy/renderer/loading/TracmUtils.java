package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tracm.TrackMaterialValueList;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.TransformStorage;

import java.util.HashMap;
import java.util.Map;

public class TracmUtils {
    public static Map<String, Animation.Offset> getOffsets(TRACM tracm) {
        var offsets = new HashMap<String, Animation.Offset>();

        if (tracm != null) {

            for (int i = 0; i < tracm.tracksLength(); i++) {
                var tracks = tracm.tracks(i);
                var materialTimeline = tracks.materialAnimation();

                if (materialTimeline == null) continue;

                for (int j = 0; j < materialTimeline.materialTrackLength(); j++) {
                    var materialTrack = materialTimeline.materialTrack(j);

                    for (int k = 0; k < materialTrack.animValuesLength(); k++) {
                        var animValues = materialTrack.animValues(k);

//                        if (animValues != null && animValues.name().equals("UVScaleOffset")) {
                        var list = animValues.list();

                        var uOffset = toStorage(list.blue());
                        var vOffset = toStorage(list.alpha());
                        var uScale = toStorage(list.green());
                        var vScale = toStorage(list.red());

                        var duration = 0.0;
                        for (var key : uOffset) duration = Math.max(key.time(), duration);
                        for (var key : vOffset) duration = Math.max(key.time(), duration);
                        for (var key : uScale) duration = Math.max(key.time(), duration);
                        for (var key : vScale) duration = Math.max(key.time(), duration);

                        offsets.putIfAbsent(materialTrack.name(), new Animation.Offset(
                                uOffset,
                                vOffset,
                                uScale,
                                vScale, (float) duration));
                    }

                }
            }
        }

        return offsets;
    }

    public static TransformStorage<Float> toStorage(TrackMaterialValueList value) {
        var storage = new TransformStorage<Float>();

        for (int i = 0; i < value.valuesLength(); i++) {
            var val = value.values(i);

            storage.add(val.time(), val.value());
        }

        return storage;
    }
}
