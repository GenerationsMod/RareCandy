package gg.generations.rarecandy.tools.walkstripper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class WalkStripper {
    public static void main(String[] args) throws IOException {
        var path = Path.of("C:\\Users\\water\\Downloads\\run.tranm");

        var buffer = ByteBuffer.wrap(Files.readAllBytes(path));
        var gfbAnim = gg.generations.rarecandy.pokeutils.tranm.Animation.getRootAsAnimation(buffer);


        gfbAnim.anim().bones(0);
    }
}
