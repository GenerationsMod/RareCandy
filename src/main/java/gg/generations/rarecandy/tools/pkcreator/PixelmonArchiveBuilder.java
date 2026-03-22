package gg.generations.rarecandy.tools.pkcreator;

import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.pokeutils.resource.PkResourceLocator;
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.resource.ResourceWriter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

/**
 * Utility for writing and reading Pixelmon: Generation's model format.
 */
public class PixelmonArchiveBuilder {
    public static void convertToPk(ResourceReader source, ResourceWriter dest, Float scale) throws IOException {
        if (scale != null && source.hasFile("config.json")) {
            var config = ModelConfig.GSON.fromJson(new String(source.getFile("config.json")), JsonObject.class);
            config.addProperty("scale", scale);
            dest.putFile("config.json", ModelConfig.GSON.toJson(config).getBytes());
        }

        for (var key : source.getFileNames()) {
            dest.putFile(key, source.getFile(key));
        }

        dest.save();
    }

    //TODO: Add more animations
    private static Optional<String> processFileName(Path archiveFile, Path relativeFolder) {
        var fileName = relativeFolder.relativize(archiveFile).getFileName().toString();
//        if (fileName.startsWith("pm") && !fileName.endsWith("png")) {
//            var cleanName = fileName.substring("pmxxxx_xx_xx_xxxxx_".length()).replace(".tranm", "").replace(".gfbanm", "");
//
//            return Optional.of(switch (cleanName) {
//                case "defaultwait01_loop" -> "idle";
//                case "battlewait01_loop" -> "battle_idle";
//                case "walk01_loop" -> "walk";
//                case "rest01_start" -> "rest_start";
//                case "rest01_loop" -> "rest_loop";
//                case "rest01_end" -> "rest_end";
//                case "roar01" -> "roar";
//                case "attack02" -> "attack";
//                case "damage02" -> "damage";
//                case "sleep01_loop" -> "sleep";
//                case "down01_start" -> "faint";
//
//                default -> "invalid_" + cleanName;
//            } + ".tranm").filter(a -> !a.startsWith("invalid"));
//        }
        return Optional.of(fileName);
    }

    public static void main(String[] args) throws IOException {
        var inFolder = Paths.get("converter/in");
        var outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);

        Files.list(inFolder).forEach(path -> {
            if (Files.isDirectory(path) || path.toString().endsWith(".glb")) {
                try {
                    var relativePath = inFolder.relativize(path);
                    var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString().replace(".glb", "") + ".pk");

                    var source = ResourceLocator.of(path);
                    var dest = new PkResourceLocator(outputPath);

                    convertToPk(source, dest, null);
                    dest.save(outputPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}