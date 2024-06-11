package gg.generations.rarecandy.pokeutils.compress;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryWriter;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnimationCompressor {

    public static void main(String[] args) throws IOException {
        Files.list(Paths.get("C:\\Users\\hydos\\Desktop\\AllPokemonSwSh\\pm0951_00_00\\Animations")).forEach(originalAnimation -> {
            if (!Files.isRegularFile(originalAnimation)) return;
            if (!originalAnimation.toFile().toString().endsWith(".smd")) return;

            try {
                var smd = Files.readString(originalAnimation);
                var smdFile = new SMDTextReader().read(smd);
                var output = originalAnimation.getParent().resolve("output").resolve(originalAnimation.getFileName().toString().replace(".smd", ".pkanm"));
                Files.createDirectories(output.getParent());
                Files.deleteIfExists(output);

                try (var packer = MessagePack.newDefaultPacker(Files.newOutputStream(output))) {
                    new SMDBinaryWriter().write(smdFile, packer);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
