package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.tranm.BoneTrackT;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Transplants {
    public static void main(String[] args) throws IOException {
        var path = Path.of("C:\\Users\\water\\Downloads\\PokesToFix\\Indigo\\Anims (ribbon)\\converted");

        var attackPath = path.resolve("pm0245_00_00_08300_loop01_loop.tranm");
        var sleepEndPath = path.resolve("sleepend.tranm");

        var attackTranm = TRANMT.deserializeFromBinary(Files.readAllBytes(attackPath));
        var sleepEndTranm = TRANMT.deserializeFromBinary(Files.readAllBytes(sleepEndPath));


        var mapattack = Stream.of(attackTranm.getTrack().getTracks()).collect(Collectors.toMap(BoneTrackT::getBoneName, a -> a));
        var mapsleep = Stream.of(sleepEndTranm.getTrack().getTracks()).collect(Collectors.toMap(BoneTrackT::getBoneName, a -> a));

        var difference = new ArrayList<>(mapattack.keySet());
        difference.removeAll(mapsleep.keySet());
        System.out.println(difference);

        var animation = sleepEndTranm.getTrack();

        var frames = animation.getTracks();

        for (int i = 0; i < frames.length; i++) {
            var frame = frames[i];

            if(mapattack.containsKey(frame.getBoneName())) {
                System.out.println("rawr: " + frame.getBoneName());
                difference.remove(frame.getBoneName());
                frames[i] = mapattack.get(frame.getBoneName());
            }
        }

        int originLenght = frames.length;

        frames = Arrays.copyOf(frames, frames.length + difference.size());

        for (int i = 0; i < difference.size(); i++) {
            System.out.println("rawr1: " + difference.get(i));
            frames[i+originLenght] = mapattack.get(difference.get(i));
        }

        animation.setTracks(frames);

        System.out.println();



        Files.write(path.resolve("test.tranm"), sleepEndTranm.serializeToBinary());
    }
}
