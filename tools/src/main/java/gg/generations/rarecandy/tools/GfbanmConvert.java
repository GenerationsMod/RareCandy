package gg.generations.rarecandy.tools;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import gg.generations.rarecandy.pokeutils.gfbanm.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks._boolean.BooleanTrackProcessor;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks._byte.ByteProcessor;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks._float.*;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.data.DataTrack;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.rotation.RotationTrackProcessor;
import gg.generations.rarecandy.pokeutils.gfbanm.tracks.vector.VectorProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class GfbanmConvert {
    private static Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(BooleanTrackProcessor.class, new BooleanTrackProcessor.Serializer())
            .registerTypeAdapter(ByteProcessor.class, new ByteProcessor.Serializer())
            .registerTypeAdapter(FloatTrackProcessor.class, new FloatTrackProcessor.Serializer())
            .registerTypeAdapter(RotationTrackProcessor.class, new RotationTrackProcessor.Serializer())
            .registerTypeAdapter(VectorProcessor.class, new VectorProcessor.Serializer())
            .registerTypeAdapter(DataTrack.class, new DataTrack.Serializer())
            .create();

    public static void main(String[] args) throws IOException {
        var in = Path.of("in");
        var out = Path.of("out");
        if(Files.notExists(in)) Files.createDirectory(in);
        if(Files.notExists(out)) Files.createDirectory(out);

        Files.walk(in).forEach(x -> {
            if(x.getFileName().toString().endsWith("gfbanm")) {
                try {
                    var animation = AnimationT.deserializeFromBinary(Files.readAllBytes(x));

                    animation.setVisibility(null);
                    animation.setEventData(null);

                    var materialAnimation = animation.getMaterial();

                    var materials = Arrays.stream(materialAnimation.getTracks())/*.dropWhile(a -> !a.getName().toLowerCase().contains("eye"))*/.toArray(MaterialTrackT[]::new);

                    for(var material : materials) {
                        material.setFlags(null);
                        material.setVectors(null);

                        var uScale = Arrays.stream(material.getValues()).filter(a -> a.getName().equals("ColorBaseU")).findFirst();
                        var uTranslate = Arrays.stream(material.getValues()).filter(a -> a.getName().equals("ColorUVTranslateU")).findFirst();

                        if(uScale.isPresent() && uTranslate.isPresent() && material.getName().toLowerCase().contains("eye")) {
                            uTranslate.get().getValue().getValue().adjustValue(0.25f);
                        } else {
                            System.out.println(uScale.isPresent() + " " + uTranslate.isPresent());
                        }

                        var array = Arrays.stream(material.getValues()).filter(a -> a.getName().startsWith("ColorUVTranslate"))/*.map(new Function<ShaderEntryT, ShaderEntryT>() {

                            @Override
                            public ShaderEntryT apply(ShaderEntryT shaderEntryT) {


                                if(shaderEntryT.getName().equals("ColorUVTranslateU")) shaderEntryT.getValue().getValue().adjustValue(0.5f);
                                return shaderEntryT;
                            }
                        })*/.toArray(ShaderEntryT[]::new);

                        material.setValues(array);
                    }

                    materialAnimation.setTracks(materials);



                    Files.write(out.resolve(x.getFileName().toString()), animation.serializeToBinary());

                    Files.writeString(out.resolve(x.getFileName().toString().replace("gfbanm", "json")), gson.toJson(animation));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

//            if(x.getFileName().toString().endsWith("json")) {
//                try {
//                    var animation = gson.fromJson(Files.readString(x), AnimationT.class);
//                    Files.write(out.resolve(x.getFileName().toString().replace("json", "gfbanm")), animation.serializeToBinary());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        });
    }


}
